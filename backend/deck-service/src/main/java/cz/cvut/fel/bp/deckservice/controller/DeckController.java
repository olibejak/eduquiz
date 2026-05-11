package cz.cvut.fel.bp.deckservice.controller;

import cz.cvut.fel.bp.deckservice.controller.util.RestUtils;
import cz.cvut.fel.bp.deckservice.dto.ErrorResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckDetailsResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckSummaryResponseDTO;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.facade.DeckServiceFacade;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

/**
 * Controller for managing decks.
 * Handles HTTP requests related to deck operations.
 * Path ../search/.. is public.
 */
@RestController
@RequestMapping("/api/decks")
@Slf4j
public class DeckController {

    private final DeckServiceFacade deckServiceFacade;

    @Autowired
    public DeckController(DeckServiceFacade deckServiceFacade) {
        this.deckServiceFacade = deckServiceFacade;
    }

    /**
     * Creates a new deck with the given request data and author ID.
     * Sends HTTP 201 Created status on success.
     * @param request the request data containing the deck information
     * @return the created deck as a response DTO with HTTP 201 Created status and path header
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DeckDetailsResponseDTO> createDeck(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DeckRequestDTO request) {
        log.info("Create deck request userId={}, title={}", userPrincipal.id(), request.title());

        DeckDetailsResponseDTO response = deckServiceFacade.createDeck(request, userPrincipal);
        log.info("Deck created userId={}, deckId={}", userPrincipal.id(), response.id());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(RestUtils.createLocationHeaderFromCurrentUri("/{id}", response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Slice<DeckSummaryResponseDTO>> getDecks(
            Pageable pageable,
            Authentication authentication
    ) {
        log.debug("Get decks request");

        // Předáme stránkování i informace o uživateli do fasády
        return ResponseEntity.ok(deckServiceFacade.getDecks(pageable, authentication));
    }

    /**
     * Retrieves a deck by its ID.
     * @param id the ID of the deck to retrieve
     * @return the retrieved deck as a response DTO
     */
    @GetMapping("/{id}")
    public DeckDetailsResponseDTO getDeckById(@PathVariable Long id) {
        log.debug("Get deck request deckId={}", id);
        return deckServiceFacade.getDeckById(id);
    }

    /**
     * Searches for decks by title keyword with pagination support.
     * @param keyword the keyword to search for in deck titles (optional)
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks matching the search criteria as response DTOs
     */
    @GetMapping("/search")
    public Slice<DeckSummaryResponseDTO> getDecksByTitle(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.debug("Search decks by title keyword={}, page={}, size={}", keyword, pageable.getPageNumber(), pageable.getPageSize());
        return deckServiceFacade.getDecksByTitle(keyword, pageable);
    }

    /**
     * Searches for decks by title keyword with pagination support.
     * @param authorName the keyword to search for in deck titles (optional)
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks matching the search criteria as response DTOs
     */
    @GetMapping(value = "/search", params = "authorName")
    public Slice<DeckSummaryResponseDTO> getDecksByAuthorName(
            @RequestParam(required = false) String authorName,
            Pageable pageable) {
        log.debug("Search decks by authorName={}, page={}, size={}", authorName, pageable.getPageNumber(), pageable.getPageSize());
        return deckServiceFacade.getDecksByAuthorName(authorName, pageable);
    }

    /**
     * Searches for decks by author ID with pagination support.
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks matching the search criteria as response DTOs
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/my")
    public Slice<DeckSummaryResponseDTO> getMyDecks(
            Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Search decks by authorId={}, page={}, size={}", userPrincipal.id(), pageable.getPageNumber(), pageable.getPageSize());
        return deckServiceFacade.getDecksByAuthorId(userPrincipal.id(), pageable);
    }

    /**
     * Searches for decks by given set of tags with pagination support.
     * @param tags the set of tags to search for
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks matching the search criteria as response DTOs
     */
    @GetMapping(value = "/search", params = "tags")
    public Slice<DeckSummaryResponseDTO> getDecksByTags(
            @RequestParam Set<DeckTagType> tags,
            Pageable pageable) {
        log.debug("Search decks by tags={}, page={}, size={}", tags, pageable.getPageNumber(), pageable.getPageSize());
        return deckServiceFacade.getDecksByTags(tags, pageable);
    }

    /**
     * Retrieves a page of decks that have been favorited by the user with the given ID.
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks that the user has favorited as response DTOs
     */
    @GetMapping("/favorites")
    public Slice<DeckSummaryResponseDTO> getFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        log.debug("Get favorites request userId={}, page={}, size={}", userPrincipal.id(), pageable.getPageNumber(), pageable.getPageSize());
        return deckServiceFacade.getFavoritedDecks(userPrincipal.id(), pageable);
    }

    /**
     * Updates an existing deck with the given ID using the provided request data.
     * @param id the ID of the deck to update
     * @param request the request data containing the updated deck information
     * @return the updated deck as a response DTO
     */
    @PutMapping("/{id}")
    public DeckDetailsResponseDTO updateDeck(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody DeckRequestDTO request) {
        log.info("Update deck request userId={}, deckId={}", userPrincipal.id(), id);
        return deckServiceFacade.updateDeck(id, request, userPrincipal);
    }

    /**
     * Deletes a deck with the given ID if the requester is the author of the deck.
     * @param id the ID of the deck to delete
     */
    @PreAuthorize("hasAnyRole('USER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeck(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        log.info("Delete deck request userId={}, deckId={}", userPrincipal.id(), id);

        deckServiceFacade.deleteDeck(id, userPrincipal);
    }

    /**
     * Toggles the favorite status of a deck for a user.
     * @param id the ID of the deck to toggle favorite status for
     * @return the updated deck after toggling the favorite status as a response DTO
     */
    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping("/{id}/favorite")
    public DeckDetailsResponseDTO toggleFavorite(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        log.info("Toggle favorite request userId={}, deckId={}", userPrincipal.id(), id);

        return deckServiceFacade.toggleFavorite(userPrincipal.id(), id);
    }
}
