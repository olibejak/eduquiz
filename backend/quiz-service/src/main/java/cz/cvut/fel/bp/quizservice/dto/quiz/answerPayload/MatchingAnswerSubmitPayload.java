package cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MatchingAnswerSubmitPayload(
        @NotNull(message = "Matches cannot be null")
        @NotEmpty(message = "Matches cannot be empty")
        Map<Long, Long> matches
) implements AnswerSubmitPayload {}
