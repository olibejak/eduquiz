package cz.cvut.fel.bp.quizservice.dto.quiz;

import lombok.Builder;

import java.util.List;

@Builder
public record QuizQuestionDTO(
        Long id,
        String text,
        String questionType,
        Integer duration,
        List<QuizAnswerDTO> answers
) {}
