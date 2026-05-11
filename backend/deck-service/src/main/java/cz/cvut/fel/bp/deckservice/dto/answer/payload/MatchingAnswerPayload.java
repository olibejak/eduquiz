package cz.cvut.fel.bp.deckservice.dto.answer.payload;

import lombok.Builder;

/**
 * Payload for matching answer.
 * @param associate - Whether the answer is an associate. Only relevant when type is "MATCHING".
 * @param matchId - The ID of the matching pair. Only relevant when type is "MATCHING".
 */
@Builder
public record MatchingAnswerPayload(
    Boolean associate,
    Integer matchId
) implements AnswerPayload {}
