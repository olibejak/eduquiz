package cz.cvut.fel.bp.deckservice.dto.answer;

import cz.cvut.fel.bp.deckservice.dto.answer.payload.AnswerPayload;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO type incoming from client when creating/updating an answer.
 * @param text - The text of the answer.
 * @param type - The type of the answer. One of: {"STANDARD", "CHOICE", "MATCHING"}
 * @param payload -The payload of the specific answer type.
 */
@Builder
public record AnswerRequestDTO(
        @NotBlank(message = "Text must not be blank")
        @Size(max = 500, message = "Text must not exceed 500 characters")
        String text,
        @NotNull(message = "Answer type must not be null")
        AnswerType type,
        AnswerPayload payload

) {}
