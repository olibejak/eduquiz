package cz.cvut.fel.bp.quizservice.service.evaluation;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;

public interface AnswerEvaluatorStrategy {
    boolean supports(AnswerSubmitDTO answerSubmitDTO);
    boolean evaluate(AnswerSubmitDTO answerSubmitDTO, QuestionDTO questionDTO);
}
