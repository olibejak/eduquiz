package cz.cvut.fel.bp.deckservice.model;

import lombok.Getter;

/**
 * Enum representing tags that can be associated with a deck.
 */
@Getter
public enum DeckTagType {

    VERIFIED("Verified"),
    SCIENCE("Science"),
    HISTORY("History"),
    GEOGRAPHY("Geography"),
    MATHEMATICS("Mathematics"),
    LANGUAGES("Languages"),
    PROGRAMMING("Programming"),
    LITERATURE("Literature"),
    POP_CULTURE("Pop Culture"),
    ART("Art"),
    OTHER("Other");

    private final String name;

    DeckTagType(String name) {
        this.name = name;
    }

}
