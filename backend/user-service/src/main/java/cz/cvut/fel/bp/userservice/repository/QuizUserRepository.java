package cz.cvut.fel.bp.userservice.repository;

import cz.cvut.fel.bp.userservice.model.UserQuizHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for UserQuizHistory entity.
 * Provides methods to perform CRUD operations and custom queries on the user quiz history data.
 */
@Repository
public interface QuizUserRepository extends JpaRepository<UserQuizHistory, Long> {

    Slice<UserQuizHistory> findByUserIdOrderByPlayedAtDesc(UUID userId, Pageable pageable);
    List<UserRepository> findAllByQuizSessionId(Long quizSessionId);
}
