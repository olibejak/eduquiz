package cz.cvut.fel.bp.quizservice.repository;

import cz.cvut.fel.bp.quizservice.model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    Optional<QuizSession> findByLobbyPin(String lobbyPin);

    boolean existsByLobbyPin(String lobbyPin);

    List<QuizSession> findAllByEmptySinceAtIsNotNullAndEmptySinceAtBefore(LocalDateTime threshold);
}