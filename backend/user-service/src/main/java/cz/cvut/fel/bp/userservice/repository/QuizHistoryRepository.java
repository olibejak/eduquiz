package cz.cvut.fel.bp.userservice.repository;

import cz.cvut.fel.bp.userservice.model.QuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserQuizHistory entity.
 * Provides methods to perform CRUD operations and custom queries on the user quiz history data.
 */
@Repository
public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {

}
