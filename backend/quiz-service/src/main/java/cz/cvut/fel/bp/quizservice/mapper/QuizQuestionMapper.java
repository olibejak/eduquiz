package cz.cvut.fel.bp.quizservice.mapper;

import cz.cvut.fel.bp.quizservice.dto.quiz.QuizAnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.QuizQuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizQuestionMapper {

    private final List<AnswerMapperStrategy> strategies;

    public QuizQuestionDTO mapToQuizQuestion(QuestionDTO questionDTO) {
        List<QuizAnswerDTO> safeAnswers = questionDTO.answers().stream()
                .map(this::mapAnswer)
                .toList();

        return new QuizQuestionDTO(
                questionDTO.id(),
                questionDTO.text(),
                questionDTO.questionType(),
                questionDTO.duration(),
                safeAnswers
        );
    }

    private QuizAnswerDTO mapAnswer(AnswerDTO answerDTO) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(answerDTO.type()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported answer type for answer ID: " + answerDTO.id()))
                .map(answerDTO);
    }
}
