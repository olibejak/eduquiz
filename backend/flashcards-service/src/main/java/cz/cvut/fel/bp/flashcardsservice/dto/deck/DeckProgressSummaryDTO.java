package cz.cvut.fel.bp.flashcardsservice.dto.deck;

import lombok.Builder;

/**
 * DTO representing the info about a deck, that is due to study.
 * Displayed at the user's home page.
 * @param id id of the due deck
 * @param title title of the due deck
 */
@Builder
public record DeckProgressSummaryDTO(
        Long id,
        String title
) {}
