package cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload;


import jakarta.validation.constraints.NotNull;

public record ChoiceAnswerSubmitPayload(
        @NotNull(message = "Answer ID cannot be null")
        Long answerId
) implements AnswerSubmitPayload {}
