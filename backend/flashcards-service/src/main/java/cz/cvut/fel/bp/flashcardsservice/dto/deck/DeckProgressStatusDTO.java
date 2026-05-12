package cz.cvut.fel.bp.flashcardsservice.dto.deck;

import lombok.Builder;

/**
 * DTO representing the study status of a deck for a user.
 * @param dueCount number of flashcards that are due for review
 * @param newCount number of new flashcards that have not been studied yet
 * @param learnedCount number of flashcards that have been learned (studied at least once)
 */
@Builder
public record DeckProgressStatusDTO(
        Integer dueCount,
        Integer newCount,
        Integer learnedCount,
        Integer totalCount
) {}
