package cz.cvut.fel.bp.deckservice.dto.deck;

import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record DeckSummaryResponseDTO(
        Long id,
        String title,
        String description,
        String authorName,
        VisibilityType visibility,
        Set<DeckTagType> tags,
        Integer favoritesCount,
        Integer numberOfQuestions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}