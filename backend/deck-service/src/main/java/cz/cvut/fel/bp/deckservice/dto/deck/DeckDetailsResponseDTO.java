package cz.cvut.fel.bp.deckservice.dto.deck;

import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO type outgoing to client when fetching a deck.
 * @param id - The ID of the deck.
 * @param title - The title of the deck.
 * @param description - The description of the deck.
 * @param visibility - The visibility of the deck. One of: {"PUBLIC", "PRIVATE"}
 * @param authorId - The ID of the author of the deck.
 * @param tags - The set of tags associated with the deck.
 * @param favoritesCount - The count of users who have favorited this deck.
 * @param questions - The list of questions associated with the deck.
 * @param createdAt - The timestamp when the deck was created.
 * @param updatedAt - The timestamp when the deck was last updated.
 */
@Builder
public record DeckDetailsResponseDTO(
        Long id,
        String title,
        String description,
        VisibilityType visibility,
        UUID authorId,
        Set<DeckTagType> tags,
        Integer favoritesCount,
        List<QuestionResponseDTO> questions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
