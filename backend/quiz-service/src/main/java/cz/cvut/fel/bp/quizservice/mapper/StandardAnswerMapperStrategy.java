package cz.cvut.fel.bp.quizservice.mapper;

import cz.cvut.fel.bp.quizservice.dto.quiz.QuizAnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;

public class StandardAnswerMapperStrategy implements AnswerMapperStrategy {

    @Override
    public boolean supports(String answerType) {
        return "standard".equalsIgnoreCase(answerType);
    }

    @Override
    public QuizAnswerDTO map(AnswerDTO answerDTO) {
        return new QuizAnswerDTO(
                answerDTO.id(),
                null,
                answerDTO.type()
        );
    }
}
