package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.client.DeckServiceClient;
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

        List<Long> questionIds = deckServiceClient.getQuestionIdsForDeck(currentDeck.getDeckId());

        if (session.getCurrentQuestionIndex() >= questionIds.size()) {
            handleEndOfDeck(session);
            return;
        }

        Long nextQuestionId = questionIds.get(session.getCurrentQuestionIndex());
        QuestionDTO questionDTO = deckServiceClient.getQuestionById(nextQuestionId);

        quizSessionService.resetAnswersForSession(pin);
        session.setState(SessionState.QUESTION_ACTIVE);
        currentDeck.setCurrentQuestionIndex(currentDeck.getCurrentQuestionIndex() + 1);
        session.setCurrentQuestionAnswersCount(0);
        quizSessionService.save(session);

        QuizQuestionDTO quizQuestion = questionMapper.mapToQuizQuestion(questionDTO);
        eventPublisher.publishQuestionStarted(pin, quizQuestion);
        // INFO: Delay question timeout by 5 seconds to allow clients to receive the event and prepare for the question
        questionTimerProvider.getObject().scheduleTimeout(pin, questionDTO.duration() + 5);
        log.info("action=moveToNextQuestion lobbyPin={} questionId={} durationScheduled={}", pin, nextQuestionId, questionDTO.duration());
    }

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
                        p.getId(),
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
        if (session.getCurrentDeckIndex() + 1 < session.getSessionDecks().size()) {
            moveToNextDeck(session);
            return;
        }
        session.setState(SessionState.FINISHED);
        quizSessionService.save(session);
        eventPublisher.publishQuizEnded(session.getLobbyPin());
    }
}
