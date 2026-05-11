package cz.cvut.fel.bp.deckservice.model;

import lombok.Getter;

/**
 * Enum representing the visibility of a deck.
 * It can be either PRIVATE or PUBLIC.
 * Might be extended in the future to include more visibility options (e.g., FRIENDS_ONLY).
 */
@Getter
public enum VisibilityType {

    PRIVATE("PRIVATE"),
    PUBLIC("PUBLIC");

    private final String value;

    VisibilityType(String value) {
        this.value = value;
    }

}
