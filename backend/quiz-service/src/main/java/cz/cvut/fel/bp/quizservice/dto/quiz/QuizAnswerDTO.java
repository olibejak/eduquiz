package cz.cvut.fel.bp.quizservice.dto.quiz;

import lombok.Builder;

@Builder
public record QuizAnswerDTO(
    Long id,
    String text,
    String type,
    Boolean associate
) {}
