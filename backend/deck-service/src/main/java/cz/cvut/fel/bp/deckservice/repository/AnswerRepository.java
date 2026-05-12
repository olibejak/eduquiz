package cz.cvut.fel.bp.deckservice.repository;

import cz.cvut.fel.bp.deckservice.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
