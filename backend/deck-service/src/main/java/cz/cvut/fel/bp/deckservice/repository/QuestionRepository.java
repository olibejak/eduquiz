package cz.cvut.fel.bp.deckservice.repository;

import cz.cvut.fel.bp.deckservice.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Integer countByDeckId(Long deckId);
    List<Question> findAllByIdIn(List<Long> ids);

    /**
     * Finds the IDs of all questions that belong to a specific deck.
     * @param deckId the ID of the deck
     * @return a list of question IDs that belong to the specified deck
     */
    @Query("SELECT q.id FROM Question q WHERE q.deck.id = :deckId")
    List<Long> findIdsByDeckId(@Param("deckId") Long deckId);

    List<Question> findAllByDeckId(Long deckId);
}
