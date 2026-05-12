package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.client.DeckServiceClient;
import cz.cvut.fel.bp.quizservice.dto.UserQuizResultsDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.QuizQuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.results.ParticipantQuestionResultsDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.results.QuestionResultsDTO;
import cz.cvut.fel.bp.quizservice.mapper.QuizQuestionMapper;
import cz.cvut.fel.bp.quizservice.model.*;
import cz.cvut.fel.bp.quizservice.service.QuizSessionService;
import cz.cvut.fel.bp.quizservice.service.util.QuestionTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuizGameFacade {

    private final DeckServiceClient deckServiceClient;
    private final QuizSessionService quizSessionService;
    private final QuizAnswerFacade quizAnswerFacade;
    private final ObjectProvider<QuestionTimer> questionTimerProvider;
    private final EventPublisherFacade eventPublisher;
    private final QuizQuestionMapper questionMapper;

    @Transactional
    public void processAnswer(AnswerSubmitDTO answerSubmitDTO) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(answerSubmitDTO.lobbyPin());
        if (session.getState() != SessionState.QUESTION_ACTIVE) return;

        quizAnswerFacade.processAnswer(answerSubmitDTO);

        if (quizSessionService.allConnectedParticipantsHaveAnswered(answerSubmitDTO.lobbyPin())) {
            endQuestion(answerSubmitDTO.lobbyPin());
        }
    }

    @Transactional
    public void moveToNextQuestion(String pin) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(pin);

        SessionDeck currentDeck = session.getSessionDecks().get(session.getCurrentDeckIndex());
        currentDeck.setCurrentQuestionIndex(currentDeck.getCurrentQuestionIndex() + 1);

        if (currentDeck.getQuestionIds().length == 0) {
            currentDeck.setQuestionIds(deckServiceClient.getQuestionIdsForDeck(currentDeck.getDeckId()).toArray(new Long[0]));
        }

        Long[] questionIds = currentDeck.getQuestionIds();

        if (session.getCurrentQuestionIndex() >= questionIds.length) {
            handleEndOfDeck(session);
            return;
        }

        Long nextQuestionId = questionIds[session.getCurrentQuestionIndex()];
        QuestionDTO questionDTO = deckServiceClient.getQuestionById(nextQuestionId);

        quizSessionService.resetAnswersForSession(pin);
        session.setState(SessionState.QUESTION_ACTIVE);
        session.setCurrentQuestionAnswersCount(0);
        quizSessionService.save(session);

        QuizQuestionDTO quizQuestion = questionMapper.mapToQuizQuestion(questionDTO);
        eventPublisher.publishQuestionStarted(pin, quizQuestion);
        // INFO: Delay question timeout by 5 seconds to allow clients to receive the event and prepare for the question
        questionTimerProvider.getObject().scheduleTimeout(pin, questionDTO.duration() + 5);
        log.info("action=moveToNextQuestion lobbyPin={} questionId={} durationScheduled={}", pin, nextQuestionId, questionDTO.duration());
    }

    @Transactional
    public void endQuestion(String pin) {
        questionTimerProvider.getObject().cancelTimer(pin);

        quizSessionService.changeStateToQuestionResults(pin);

        publishResultsEvent(pin);
    }

    private void publishResultsEvent(String pin) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(pin);

        QuestionResultsDTO results = new QuestionResultsDTO(
                fetchQuestionData(session.getCurrentQuestionId()),
                mapParticipantResults(session)
        );

        eventPublisher.publishQuestionEnded(pin, results);
    }

    private QuestionDTO fetchQuestionData(Long questionId) {
        return deckServiceClient.getQuestionById(questionId);
    }

    private List<ParticipantQuestionResultsDTO> mapParticipantResults(QuizSession session) {
        return session.getParticipants().stream()
                .map(p -> new ParticipantQuestionResultsDTO(
                        p.getId(), p.getNickname(),
        p.getIsCurrentCorrect() != null ? p.getIsCurrentCorrect() : false,
        p.getCurrentScore()
                ))
                .toList();
    }

    private void moveToNextDeck(QuizSession session) {
        session.setCurrentDeckIndex(session.getCurrentDeckIndex() + 1);
        quizSessionService.save(session);
    }

    private void handleEndOfDeck(QuizSession session) {
        if (session.getCurrentDeckIndex() + 1 >= session.getSessionDecks().size()) {
            handleEndOfQuiz(session);
            return;
        }
        moveToNextDeck(session);
    }

    private void handleEndOfQuiz(QuizSession session) {
        session.setState(SessionState.FINISHED);
        quizSessionService.save(session);
        List<UserQuizResultsDTO> results = getResults(session);

        List<Long> orderedDeckIds = session.getSessionDecks().stream()
                .sorted(Comparator.comparing(SessionDeck::getPlayOrder))
                .map(SessionDeck::getDeckId)
                .toList();

        java.util.Map<Long, String> deckNamesMap = deckServiceClient.getDeckNames(orderedDeckIds);
        List<String> deckNames = orderedDeckIds.stream()
                .map(id -> deckNamesMap.getOrDefault(id, "Deleted"))
                .toList();

        eventPublisher.publishQuizEnded(session.getLobbyPin(), results, deckNames);
    }

    private List<UserQuizResultsDTO> getResults(QuizSession session) {
        List<QuizParticipant> sortedParticipants = session.getParticipants().stream()
                .sorted(Comparator.comparing(QuizParticipant::getCurrentScore).reversed())
                .toList();

        List<UserQuizResultsDTO> results = new ArrayList<>();
        int currentPosition = 1;

        for (int i = 0; i < sortedParticipants.size(); i++) {
            QuizParticipant p = sortedParticipants.get(i);

            if (i > 0 && p.getCurrentScore() < sortedParticipants.get(i - 1).getCurrentScore()) {
                currentPosition = i + 1;
            }

            results.add(new UserQuizResultsDTO(
                    p.getUserId(),
                    p.getNickname(),
                    p.getCurrentScore(),
                    currentPosition
            ));
        }

        return results;
    }
}
