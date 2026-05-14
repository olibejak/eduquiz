package cz.cvut.fel.bp.flashcardsservice.service.event;

public record DeckDeletedEvent(
        Long deckId
) {}
