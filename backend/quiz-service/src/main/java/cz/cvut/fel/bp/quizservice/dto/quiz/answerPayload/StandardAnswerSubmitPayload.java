package cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StandardAnswerSubmitPayload(
        @NotNull
        String answerType,
        @NotBlank(message = "Answer text cannot be blank")
        String text
) implements AnswerSubmitPayload {}
