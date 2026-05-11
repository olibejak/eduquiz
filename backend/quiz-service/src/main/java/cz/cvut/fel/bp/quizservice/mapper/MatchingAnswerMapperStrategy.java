package cz.cvut.fel.bp.quizservice.mapper;

import cz.cvut.fel.bp.quizservice.dto.quiz.QuizAnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;
import org.springframework.stereotype.Component;

@Component
public class MatchingAnswerMapperStrategy implements AnswerMapperStrategy {

    @Override
    public boolean supports(String answerType) {
        return "matching".equalsIgnoreCase(answerType);
    }

    @Override
    public QuizAnswerDTO map(AnswerDTO answerDTO) {
        return new QuizAnswerDTO(
                answerDTO.id(),
                answerDTO.text(),
                answerDTO.type()
        );
    }
}
