package cz.cvut.fel.bp.quizservice.service.evaluation;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.StandardAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import jakarta.persistence.Column;
import org.springframework.stereotype.Component;

@Component
public class StandardEvaluatorStrategy implements AnswerEvaluatorStrategy {

    @Override
    public boolean supports(AnswerSubmitDTO answerSubmitDTO) {
        return answerSubmitDTO.answerType().equals("STANDARD")
                && answerSubmitDTO.payload() instanceof StandardAnswerSubmitPayload;
    }

    @Override
    public boolean evaluate(AnswerSubmitDTO answerSubmitDTO, QuestionDTO questionDTO) {
        StandardAnswerSubmitPayload answerSubmitPayload = (StandardAnswerSubmitPayload) answerSubmitDTO.payload();

        return questionDTO.answers().stream()
                .anyMatch(answer -> answer.text().equals(answerSubmitPayload.text()));
    }
}
