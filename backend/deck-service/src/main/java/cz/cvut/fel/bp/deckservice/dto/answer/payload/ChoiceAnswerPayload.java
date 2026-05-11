package cz.cvut.fel.bp.deckservice.dto.answer.payload;

import lombok.Builder;

/**
 * Payload for a choice answer.
 * @param isCorrect - Whether the answer is correct. Only relevant when type is "CHOICE".
 */
@Builder
public record ChoiceAnswerPayload(
    Boolean isCorrect
) implements AnswerPayload {}
