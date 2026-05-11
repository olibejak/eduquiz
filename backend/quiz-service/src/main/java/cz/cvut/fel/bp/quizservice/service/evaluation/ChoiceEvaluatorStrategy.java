package cz.cvut.fel.bp.quizservice.service.evaluation;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.ChoiceAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.question.answerPayload.ChoiceAnswerPayload;
import org.springframework.stereotype.Component;

@Component
public class ChoiceEvaluatorStrategy implements AnswerEvaluatorStrategy {

    @Override
    public boolean supports(AnswerSubmitDTO answerSubmitDTO) {
        return answerSubmitDTO.answerType().equals("CHOICE")
                && answerSubmitDTO.payload() instanceof ChoiceAnswerSubmitPayload;
    }

    @Override
    public boolean evaluate(AnswerSubmitDTO answerSubmitDTO, QuestionDTO questionDTO) {
        ChoiceAnswerSubmitPayload answerSubmitPayload = (ChoiceAnswerSubmitPayload) answerSubmitDTO.payload();

        return questionDTO.answers().stream()
                .filter(answer -> answer.payload() instanceof ChoiceAnswerPayload(Boolean isCorrect)
                        && Boolean.TRUE.equals(isCorrect))
                .map(AnswerDTO::id)
                .anyMatch(answerSubmitPayload.answerId()::equals);
    }
}