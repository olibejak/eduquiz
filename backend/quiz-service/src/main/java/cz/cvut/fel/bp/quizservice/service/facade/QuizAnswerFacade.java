package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.client.DeckServiceClient;
import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.service.QuizParticipantService;
import cz.cvut.fel.bp.quizservice.service.evaluation.AnswerEvaluatorStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizAnswerFacade {

    private final QuizParticipantFacade participantServiceFacade;
    private final QuizParticipantService participantService;
    private final DeckServiceClient deckServiceClient;

    private final List<AnswerEvaluatorStrategy> evaluators;

    public void processAnswer(AnswerSubmitDTO answerSubmitDTO) {
        if (participantService.didAlreadyAnswer(answerSubmitDTO.participantId()))
            throw new AccessDeniedException("Participant has already submitted an answer for this question.");

        log.debug("action=processAnswer lobbyPin={} participantId={} questionId={} answerType={}",
                answerSubmitDTO.lobbyPin(), answerSubmitDTO.participantId(), answerSubmitDTO.questionId(),
                answerSubmitDTO.answerType()
        );

        QuizParticipant participant = participantServiceFacade.findById(
                answerSubmitDTO.participantId(), answerSubmitDTO.lobbyPin());

        QuestionDTO dbQuestion = deckServiceClient.getQuestionById(answerSubmitDTO.questionId());

        boolean isCorrect = evaluate(answerSubmitDTO, dbQuestion);

        // Todo: possible scoring mechanism (strategy)
        int points = isCorrect ? 1 : 0;

        participantServiceFacade.evaluate(participant.getId(), points, isCorrect);

        log.info("action=processAnswer lobbyPin={} participantId={} questionId={} answerType={} isCorrect={} " +
                "pointsEarned={}", answerSubmitDTO.lobbyPin(), participant.getId(),
                answerSubmitDTO.questionId(), answerSubmitDTO.answerType(), isCorrect, points);
    }

    public boolean evaluate(AnswerSubmitDTO answerSubmitDTO,  QuestionDTO questionDTO) {
        return evaluators.stream()
                .filter(e -> e.supports(answerSubmitDTO))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn(
                            "action=processAnswer lobbyPin={} participantId={} questionId={} answerType={}" +
                                    " reason=unsupportedAnswerType", answerSubmitDTO.lobbyPin(),
                            answerSubmitDTO.participantId(), answerSubmitDTO.questionId(), answerSubmitDTO.answerType()
                    );
                    return new IllegalArgumentException("Invalid answer type");
                }).evaluate(answerSubmitDTO, questionDTO);
    }
}
