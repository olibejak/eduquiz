package cz.cvut.fel.bp.flashcardsservice.dto;

import cz.cvut.fel.bp.flashcardsservice.dto.payload.AnswerPayload;
import lombok.Builder;

/**
 * DTO type outgoing to client when retrieving an answer.
 * @param id - The ID of the answer.
 * @param text - The text of the answer.
 * @param type - The type of the answer. One of: {"STANDARD", "CHOICE", "MATCHING"}
 * @param payload - The payload of the specific answer type.
 */
@Builder
public record AnswerDTO(
        Long id,
        String text,
        String type,
        AnswerPayload payload
) {}
