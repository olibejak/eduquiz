package cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload;

import jakarta.validation.constraints.NotBlank;

public record StandardAnswerSubmitPayload(
        @NotBlank(message = "Answer text cannot be blank")
        String text
) implements AnswerSubmitPayload {}
