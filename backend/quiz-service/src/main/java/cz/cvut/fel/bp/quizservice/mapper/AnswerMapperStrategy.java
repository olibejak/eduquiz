package cz.cvut.fel.bp.quizservice.mapper;

import cz.cvut.fel.bp.quizservice.dto.quiz.QuizAnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;

public interface AnswerMapperStrategy {
    boolean supports(String type);
    QuizAnswerDTO map(AnswerDTO answerDTO);
}
