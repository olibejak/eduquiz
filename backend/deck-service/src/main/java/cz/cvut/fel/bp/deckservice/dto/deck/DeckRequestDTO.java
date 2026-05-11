package cz.cvut.fel.bp.deckservice.dto.deck;

import cz.cvut.fel.bp.deckservice.dto.question.QuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.util.Set;

/**
 * DTO type incoming from client when creating/updating a deck.
 * @param title - The title of the deck.
 * @param description - The description of the deck.
 * @param visibility - The visibility of the deck. One of: {"PUBLIC", "PRIVATE"}
 * @param tags - The set of tags associated with the deck.
 */
@Builder
public record DeckRequestDTO(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,
        @NotNull(message = "Visibility must not be null")
        VisibilityType visibility,
        @NotNull
        Set<DeckTagType> tags,
        @Valid
        List<QuestionRequestDTO> questions
) {}
