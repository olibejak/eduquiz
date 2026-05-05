package cz.cvut.fel.bp.deckservice.dto.answer;

import cz.cvut.fel.bp.deckservice.dto.answer.payload.AnswerPayload;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import lombok.Builder;

/**
 * DTO type outgoing to client when retrieving an answer.
 * @param id - The ID of the answer.
 * @param text - The text of the answer.
 * @param type - The type of the answer. One of: {"STANDARD", "CHOICE", "MATCHING"}
 * @param payload - The payload of the specific answer type.
 */
@Builder
public record AnswerResponseDTO(
        Long id,
        String text,
        AnswerType type,
        AnswerPayload payload
) {}
