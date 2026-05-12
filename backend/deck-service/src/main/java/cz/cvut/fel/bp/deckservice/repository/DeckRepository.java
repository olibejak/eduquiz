package cz.cvut.fel.bp.deckservice.repository;

import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    Slice<Deck> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Slice<Deck> findAllByAuthorId(UUID authorId, Pageable pageable);
    Slice<Deck> findByFavoritedByUsersContains(UUID userId, Pageable pageable);
    Slice<Deck> findByAuthorIdIn(List<UUID> authorIds, Pageable pageable);
    Slice<Deck> findByTagsIn(Set<DeckTagType> tags, Pageable pageable);
    Slice<Deck> findAllByVisibility(VisibilityType visibility, Pageable pageable);
    Slice<Deck> findAllByVisibilityOrAuthorId(VisibilityType visibility, UUID authorId, Pageable pageable);

    @Query("SELECT d FROM Deck d JOIN d.favoritedByUsers u WHERE u = :userId AND d.visibility = 'PUBLIC' OR d.authorId = u")
    Slice<Deck> findAllPublicOrOwnFavorites(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds the name of a deck by its ID.
     * @param deckId the ID of the deck
     * @return the name of the deck, or null if not found
     */
    @Query("SELECT d.title FROM Deck d WHERE d.id = :deckId")
    String findNameById(@Param("deckId") Long deckId);

    /**
     * Finds the names of decks by their IDs.
     * @param deckIds the list of deck IDs
     * @return a list of DeckIdAndName projections containing the IDs and names of the decks
     */
    @Query("SELECT d.id AS id, d.title AS name FROM Deck d WHERE d.id IN :deckIds")
    List<DeckIdAndName> findNamesByIds(@Param("deckIds") List<Long> deckIds);

    /**
     * Projection interface for fetching deck ID and name.
     */
    interface DeckIdAndName {
        Long getId();
        String getTitle();
    }
}
