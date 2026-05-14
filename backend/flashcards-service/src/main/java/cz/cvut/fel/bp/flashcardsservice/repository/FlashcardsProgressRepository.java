package cz.cvut.fel.bp.flashcardsservice.repository;

import cz.cvut.fel.bp.flashcardsservice.model.FlashcardProgress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FlashcardsProgressRepository extends JpaRepository<FlashcardProgress, Long> {

    Optional<FlashcardProgress> findByUserIdAndQuestionId(UUID userId, Long questionId);
    List<FlashcardProgress> findByUserIdAndQuestionIdIn(UUID userId, List<Long> questionIds);
    Integer countByUserIdAndDeckId(UUID userId, Long deckId);

    @Query("SELECT p FROM FlashcardProgress p WHERE p.userId = :userId AND p.deckId = :deckId AND p.questionId IN :questionIds")
    List<FlashcardProgress> findAllExistingProgress(
            @Param("userId") UUID userId,
            @Param("deckId") Long deckId,
            @Param("questionIds") List<Long> questionIds);

    @Query("SELECT p.questionId FROM FlashcardProgress p WHERE p.userId = :userId AND p.deckId = :deckId AND p.nextReviewAt <= :now ORDER BY p.nextReviewAt ASC")
    List<Long> findDueQuestionIds(
            @Param("userId") UUID userId,
            @Param("deckId") Long deckId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT p.questionId FROM FlashcardProgress p WHERE p.userId = :userId AND p.deckId = :deckId")
    Set<Long> findAllSeenQuestionIds(
            @Param("userId") UUID userId,
            @Param("deckId") Long deckId
    );

    /**
     * Find the IDs of the 10 hardest cards for a specific user, based on their ratings.
     * @param userId ID of the user for whom to find the hardest cards
     * @return List of question IDs corresponding to the 10 hardest cards, ordered by userRating ascending (hardest first)
     */
    @Query("SELECT fp.questionId FROM FlashcardProgress fp WHERE fp.userId = :userId ORDER BY fp.userRating ASC LIMIT 10")
    List<Long> findHardestCardsForCramming(@Param("userId") UUID userId);

    /**
     * Count the number of cards that are due for review for a specific user and deck.
     * @param userId ID of the user for whom to count due cards
     * @param deckId ID of the deck for which to count due cards
     * @param now Current date and time to compare against the nextReviewAt field
     * @return Number of cards that are due for review for the specified user and deck, where nextReviewAt is less than or equal to the current date and time
     */
    @Query("SELECT COUNT(p) FROM FlashcardProgress p WHERE p.userId = :userId AND p.deckId = :deckId AND p.nextReviewAt <= :now")
    Integer countDueCardsForDeck(@Param("userId") UUID userId, @Param("deckId") Long deckId, @Param("now") LocalDateTime now);

    /**
     * Find deck IDs that have the most due cards for a specific user, ordered by the number of due cards in descending order.
     * @param userId ID of the user for whom to find priority decks
     * @param now Current date and time to compare against the nextReviewAt field
     * @param pageable Pagination information to limit the number of results returned
     * @return Slice of deck IDs that have the most due cards for the specified user, ordered by the number of due cards in descending order
     */
    @Query("SELECT p.deckId FROM FlashcardProgress p WHERE p.userId = :userId AND p.nextReviewAt <= :now GROUP BY p.deckId ORDER BY COUNT(p) DESC")
    Slice<Long> findPriorityDeckIds(@Param("userId") UUID userId,
                                    @Param("now") LocalDateTime now,
                                    Pageable pageable);

    void deleteByUserIdAndDeckId(UUID id, Long deckId);

    void deleteByDeckId(Long deckId);

    void deleteAllByUserId(UUID userId);
}
