package cz.cvut.fel.bp.quizservice.dto.quiz;

import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.AnswerSubmitPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerSubmitDTO(
        @NotBlank(message = "Lobby PIN cannot be blank")
        String lobbyPin,
        @NotNull(message = "Participant ID cannot be null")
        Long participantId,
        @NotNull(message = "Question ID cannot be null")
        Long questionId,
        @NotNull(message = "Answer type cannot be null")
        String answerType,
        @NotNull(message = "Answer payload cannot be null")
        AnswerSubmitPayload payload
) {}
