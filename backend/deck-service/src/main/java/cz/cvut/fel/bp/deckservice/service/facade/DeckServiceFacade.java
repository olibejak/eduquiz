package cz.cvut.fel.bp.deckservice.service.facade;

import cz.cvut.fel.bp.deckservice.dto.deck.DeckDetailsResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckSummaryResponseDTO;
import cz.cvut.fel.bp.deckservice.mapper.facade.MapperFacade;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.DeckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Facade pattern between DeckService and DeckController.
 * Primary maps between DTOs and entities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeckServiceFacade {

    private final DeckService deckService;
    private final MapperFacade mapperFacade;

    /**
     * Creates a new deck with the given request data and author ID.
     * @param request the request data containing the deck information
     * @param userPrincipal the authenticated user principal containing the author's information
     * @return the created deck as a response DTO
     */
    // Todo: refactor question adding so the method has single responsibility
    @Transactional
    public DeckDetailsResponseDTO createDeck(DeckRequestDTO request, UserPrincipal userPrincipal) {
        removeVerifiedIfNotRoleAdmin(request);
        log.debug("Create deck in facade userId={}", userPrincipal.id());
        Deck deckToCreate = mapperFacade.toDeckEntity(request);
        Deck savedDeck = deckService.createDeck(userPrincipal.id(), deckToCreate);
        log.debug("Create deck in facade completed userId={}, deckId={}", userPrincipal.id(), savedDeck.getId());
        return mapperFacade.toDeckDetailsResponse(savedDeck);
    }

    /**
     * Retrieves a deck by its ID.
     * @param id the ID of the deck to retrieve
     * @return the retrieved deck as a response DTO
     */
    @Transactional(readOnly = true)
    public DeckDetailsResponseDTO getDeckById(Long id) {
        log.debug("Get deck in facade deckId={}", id);
        Deck deck = deckService.getDeckById(id);
        return mapperFacade.toDeckDetailsResponse(deck);
    }

    /**
     * Searches for decks by title keyword with pagination support.
     * @param keyword the keyword to search for in deck titles (optional)
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks matching the search criteria as response DTOs
     */
    @Transactional(readOnly = true)
    public Slice<DeckSummaryResponseDTO> getDecksByTitle(String keyword, Pageable pageable) {
        log.debug("Search decks in facade by title keyword={}", keyword);
        Slice<Deck> entityPage = deckService.getDecksByTitle(keyword, pageable);

        return mapperFacade.toDeckSummarySlice(entityPage);
    }

    /**
     * Searches for decks by tags with pagination support.
     * @param tags the set of tags to search for
     * @param pageable the pagination information
     * @return a page of decks matching the specified tags as response DTOs
     */
    @Transactional
    public Slice<DeckSummaryResponseDTO> getDecksByTags(Set<DeckTagType> tags, Pageable pageable) {
        log.debug("Search decks in facade by tags={}", tags);
        Slice<Deck> entityPage = deckService.getDecksByTags(tags, pageable);

        return mapperFacade.toDeckSummarySlice(entityPage);
    }

    /**
     * Updates an existing deck with the given ID using the provided request data.
     * @param deckId the ID of the deck to update
     * @param request the request data containing the updated deck information
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @return the updated deck as a response DTO
     */
    @Transactional
    public DeckDetailsResponseDTO updateDeck(Long deckId, DeckRequestDTO request, UserPrincipal userPrincipal) {
        removeVerifiedIfNotRoleAdmin(request);
        log.debug("Update deck in facade userId={}, deckId={}", userPrincipal.id(), deckId);
        Deck existingDeck = deckService.getDeckById(deckId);
        deckService.verifyOwnership(existingDeck, userPrincipal);
        mapperFacade.updateDeckFromRequest(request, existingDeck);
        Deck updatedDeck = deckService.saveDeck(existingDeck);
        log.debug("Update deck in facade completed userId={}, deckId={}", userPrincipal.id(), updatedDeck.getId());
        return mapperFacade.toDeckDetailsResponse(updatedDeck);
    }

    /**
     * Deletes a deck with the given ID if the requester is the author of the deck.
     * @param deckId the ID of the deck to delete
     * @param userPrincipal the authenticated user principal containing the requester's information
     */
    @Transactional
    public void deleteDeck(Long deckId, UserPrincipal userPrincipal) {
        log.debug("Delete deck in facade userId={}, deckId={}", userPrincipal.id(), deckId);
        deckService.deleteDeck(deckId, userPrincipal);
    }

    /**
     * Toggles the favorite status of a deck for a user.
     * @param userId the ID of the user toggling the favorite status
     * @param deckId the ID of the deck to toggle favorite status for
     * @return the updated deck after toggling the favorite status as a response DTO
     */
    @Transactional
    public DeckDetailsResponseDTO toggleFavorite(UUID userId, Long deckId) {
        log.debug("Toggle favorite in facade userId={}, deckId={}", userId, deckId);
        Deck deck = deckService.toggleFavorite(userId, deckId);
        return mapperFacade.toDeckDetailsResponse(deck);
    }

    /**
     * Retrieves a page of decks created by a specific author.
     * @param authorId the ID of the author whose decks to retrieve
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks created by the specified author as response DTOs
     */
    @Transactional(readOnly = true)
    public Slice<DeckSummaryResponseDTO> getDecksByAuthorId(UUID authorId, Pageable pageable) {
        log.debug("Search decks in facade by authorId={}", authorId);
        Slice<Deck> entityPage = deckService.getDecksByAuthor(authorId, pageable);

        return mapperFacade.toDeckSummarySlice(entityPage);
    }

    /**
     * Retrieves a page of decks created by authors whose names match the given author name.
     * @param authorName the name of the author to search for (partial match, case-insensitive)
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks created by authors matching the specified name as response DTOs
     */
    @Transactional(readOnly = true)
    public Slice<DeckSummaryResponseDTO> getDecksByAuthorName(String authorName, Pageable pageable) {
        log.debug("Search decks in facade by authorName={}", authorName);
        return mapperFacade.toDeckSummarySlice(deckService.getDecksByAuthorIds(
                mapperFacade.getUserIdsByAuthorName(authorName), pageable));
    }

    /**
     * Retrieves a page of decks that have been favorited by a specific user.
     * @param userId the ID of the user whose favorited decks to retrieve
     * @param pageable the pagination information (page number, size, sorting)
     * @return a page of decks favorited by the specified user as response DTOs
     */
    @Transactional(readOnly = true)
    public Slice<DeckSummaryResponseDTO> getFavoritedDecks(UUID userId, Pageable pageable) {
        log.debug("Get favorites in facade userId={}", userId);
        Slice<Deck> entityPage = deckService.getFavoritedDecks(userId, pageable);

        return mapperFacade.toDeckSummarySlice(entityPage);
    }

    /**
     * Retrieves the name of a deck given its ID.
     * @param deckId the ID of the deck to retrieve the name for
     * @return the name of the deck with the specified ID
     */
    public String getDeckName(Long deckId) {
        return deckService.getDeckById(deckId).getTitle();
    }

    /**
     * Retrieves a map of deck IDs to their corresponding names for the given list of deck IDs.
     * @param deckIds the list of deck IDs to retrieve names for
     * @return a map where the keys are deck IDs and the values are the corresponding deck names
     */
    public Map<Long, String> getDeckNames(List<Long> deckIds) {
        return deckIds.stream()
                .map(deckService::getDeckById)
                .collect(Collectors.toMap(
                        Deck::getId,
                        Deck::getTitle,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * Retrieves the total number of questions in a deck given its ID.
     * @param deckId the ID of the deck to retrieve the question count for
     * @return the total number of questions in the deck with the specified ID, or 0 if the deck has no questions
     */
    public Integer getTotalQuestionsCount(Long deckId) {
        Deck deck = deckService.getDeckById(deckId);
        return deck.getQuestions() == null ? 0 : deck.getQuestions().size();
    }

    /**
     * Retrieves a list of question IDs for all questions in a deck given its ID.
     * @param deckId the ID of the deck to retrieve question IDs for
     * @return a list of question IDs for all questions in the deck with the specified ID, or an empty list if the deck has no questions
     */
    public List<Long> getAllQuestionIdsForDeck(Long deckId) {
        Deck deck = deckService.getDeckById(deckId);
        return deck.getQuestions() == null
                ? List.of()
                : deck.getQuestions().stream().map(Question::getId).toList();
    }

    // Todo: refactor each possibility to own method
    public Slice<DeckSummaryResponseDTO> getDecks(Pageable pageable, Authentication authentication) {
        log.debug("Get decks in facade");
        Slice<Deck> entityPage = Page.empty(pageable);
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
             entityPage = deckService.getAllPublicDecks(pageable);
        } else {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

            if (isAdmin) {
                entityPage = deckService.getDecks(pageable);
            } else {
                entityPage = deckService.getPublicAndUserDecks(((UserPrincipal) Objects.requireNonNull(authentication.getPrincipal())).id(), pageable);
            }
        }
        return mapperFacade.toDeckSummarySlice(entityPage);
    }

    private void removeVerifiedIfNotRoleAdmin(DeckRequestDTO request) {
        if (request.tags().contains(DeckTagType.VERIFIED)) {
            boolean isAdmin = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getAuthorities()
                    .stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

            if (!isAdmin) {
                request.tags().remove(DeckTagType.VERIFIED);
            }
        }
    }
}
