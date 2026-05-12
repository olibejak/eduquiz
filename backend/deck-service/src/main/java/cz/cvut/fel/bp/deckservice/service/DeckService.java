package cz.cvut.fel.bp.deckservice.service;

import cz.cvut.fel.bp.deckservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import cz.cvut.fel.bp.deckservice.repository.DeckRepository;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.validation.QuestionAnswerValidator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service class for managing Deck entities.
 * Provides methods for creating and retrieving decks.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final QuestionAnswerValidator questionAnswerValidator;

    /**
     * Creates a new deck with the given author ID and request data.
     * @param authorId the ID of the author creating the deck
     * @param request the request data containing the deck information
     * @return the created deck
     */
    @Transactional
    public Deck createDeck(@NonNull UUID authorId, @NonNull Deck request) {
        log.debug("Create deck in service authorId={}", authorId);
        request.setAuthorId(authorId);
        return saveDeck(request);
    }

    /**
     * Saves the given deck to the repository.
     * @param request the deck to save
     * @return the saved deck
     */
    @Transactional
    public Deck saveDeck(Deck request) {
        log.debug("Save deck deckId={}, authorId={}", request.getId(), request.getAuthorId());
        validateDeckQuestions(request);
        return deckRepository.save(request);
    }

    /**
     * Validates all questions in a deck.
     * @param deck the deck containing questions to validate
     */
    private void validateDeckQuestions(Deck deck) {
        if (deck.getQuestions() != null) {
            deck.getQuestions().forEach(questionAnswerValidator::validateQuestionAnswers);
        }
    }

    /**
     * Deletes a deck with the given ID if the requester is the author of the deck.
     * @param deckId the ID of the deck to delete
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @throws ResourceNotFoundException if no deck with the given ID exists
     */
    @Transactional
    public void deleteDeck(Long deckId, UserPrincipal userPrincipal) {
        log.debug("Delete deck in service userId={}, deckId={}", userPrincipal.id(), deckId);
        Deck deck = getDeckById(deckId);
        verifyOwnership(deck, userPrincipal);
        deckRepository.delete(deck);
        log.info("Deck deleted userId={}, deckId={}", userPrincipal.id(), deckId);
    }

    /**
     * Retrieves a deck by its ID.
     * @param deckId the ID of the deck to retrieve
     * @return the retrieved deck
     * @throws ResourceNotFoundException if no deck with the given ID exists
     */
    @Transactional(readOnly = true)
    public Deck getDeckById(Long deckId) {
        log.debug("Load deck deckId={}", deckId);
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck", deckId));
    }

    /**
     * Searches for decks that contain the given keyword in their title (case-insensitive).
     * @param keyword the keyword to search for in deck titles
     * @param pageable the pagination information for the search results
     * @return a page of decks that match the search criteria,
     * or an empty list if the keyword is null, empty, or if no decks match
     */
    @Transactional(readOnly = true)
    public Slice<Deck> getDecksByTitle(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("Skip deck title search because keyword is empty");
            return Page.empty(pageable);
        }
        log.debug("Search decks by title keyword={}", keyword.trim());
        return deckRepository.findByTitleContainingIgnoreCase(keyword.trim(), pageable);
    }

    /**
     * Toggles the favorite status of a deck for a given user.
     * If the user has already favorited the deck, it will be removed from their favorites;
     * otherwise, it will be added.
     * @param deckId the ID of the deck to toggle favorite status for
     * @param userId the ID of the user toggling the favorite status
     * @return the updated deck after toggling the favorite status
     * @throws ResourceNotFoundException if no deck with the given ID exists
     */
    @Transactional
    public Deck toggleFavorite(UUID userId, Long deckId) {
        Deck deck = getDeckById(deckId);
        if (deck.getFavoritedByUsers().contains(userId)) {
            deck.getFavoritedByUsers().remove(userId);
            log.info("Favorite removed userId={}, deckId={}", userId, deckId);
        } else {
            deck.getFavoritedByUsers().add(userId);
            log.info("Favorite added userId={}, deckId={}", userId, deckId);
        }
        return saveDeck(deck);
    }

    /**
     * Retrieves all decks created by a specific author.
     * @param authorId the ID of the author whose decks to retrieve
     * @param pageable the pagination information for the search results
     * @return a page of decks created by the specified author
     */
    @Transactional(readOnly = true)
    public Slice<Deck> getDecksByAuthor(UUID authorId, Pageable pageable) {
        log.debug("Search decks by authorId={}", authorId);
        return deckRepository.findAllByAuthorId(authorId, pageable);
    }

    /**
     * Retrieves all decks that have been favorited by a specific user.
     * @param userId the ID of the user whose favorited decks to retrieve
     * @param pageable the pagination information for the search results
     * @return a list of decks favorited by the specified user
     */
    @Transactional(readOnly = true)
    public Slice<Deck> getFavoritedDecks(UUID userId, Pageable pageable) {
        log.debug("Search favorite decks by userId={}", userId);
        return deckRepository.findAllPublicOrOwnFavorites(userId, pageable);
    }

    /**
     * Retrieves all decks that are tagged with any of the specified tags.
     * @param tags the set of tags to search for in decks
     * @param pageable the pagination information for the search results
     * @return a page of decks that have any of the specified tags
     */
    @Transactional(readOnly = true)
    public Slice<Deck> getDecksByTags(Set<DeckTagType> tags, Pageable pageable) {
        if (tags == null || tags.isEmpty()) {
            log.debug("Skip deck tag search because tags are empty");
            return Page.empty(pageable);
        }
        log.debug("Search decks by tags={}", tags);
        return deckRepository.findByTagsIn(tags, pageable);
    }

    /**
     * Retrieves all decks created by any of the authors in the given list of author IDs.
     * @param authorIds the list of author IDs whose decks to retrieve
     * @param pageable the pagination information for the search results
     * @return a page of decks or an empty page
     */
    @Transactional(readOnly = true)
    public Slice<Deck> getDecksByAuthorIds(List<UUID> authorIds, Pageable pageable) {
        if (authorIds == null || authorIds.isEmpty()) {
            log.debug("Skip deck authorIds search because authorIds are empty");
            return Page.empty(pageable);
        }
        log.debug("Search decks by authorIdsCount={}", authorIds.size());
        return deckRepository.findByAuthorIdIn(authorIds, pageable);
    }

    /**
     * Verifies that the requester is the author of the given deck.
     * @param deck the deck to check ownership of
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @throws AccessDeniedException if the requester is not the author of the deck or admin
     */
    @Transactional(readOnly = true)
    public void verifyOwnership(Deck deck, UserPrincipal userPrincipal) {
        boolean isAdmin = "ROLE_ADMIN".equals(userPrincipal.role());
        boolean isAuthor = deck.getAuthorId().equals(userPrincipal.id());

        if (!isAdmin && !isAuthor) {
            log.warn("Ownership verification failed userId={}, deckId={}, userRole={}",
                    userPrincipal.id(), deck.getId(), userPrincipal.role());
            throw new AccessDeniedException("You do not have permission to perform this operation on this deck.");
        }
        log.debug("Ownership verified userId={}, deckId={}", userPrincipal.id(), deck.getId());
    }

    @Transactional(readOnly = true)
    public Slice<Deck> getDecks(Pageable pageable) {
        log.debug("Get all decks page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return deckRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Deck> getAllPublicDecks(Pageable pageable) {
        log.debug("Get all Public decks page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return deckRepository.findAllByVisibility(VisibilityType.PUBLIC ,pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Deck> getPublicAndUserDecks(UUID authorId, Pageable pageable) {
        log.debug("Get public and user decks page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return deckRepository.findAllByVisibilityOrAuthorId(VisibilityType.PUBLIC, authorId, pageable);
    }
}
