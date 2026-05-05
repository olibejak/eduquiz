package cz.cvut.fel.bp.flashcardsservice.dto;

import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;

public record FlashcardReviewDTO(
        Long questionId,
        FlashcardRating rating
) {}
