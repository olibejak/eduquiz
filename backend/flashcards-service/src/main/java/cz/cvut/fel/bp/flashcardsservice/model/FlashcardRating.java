package cz.cvut.fel.bp.flashcardsservice.model;

import lombok.Getter;

@Getter
public enum FlashcardRating {
    AGAIN(0),
    HARD(1),
    GOOD(2),
    EXCELLENT(3);

    private final int value;

    FlashcardRating(int value) {
        this.value = value;
    }

}
