package cz.cvut.fel.bp.flashcardsservice.dto;

import java.util.List;

public record FlashcardReviewBatchDTO(
        List<FlashcardReviewDTO> reviews
) {}
