package cz.cvut.fel.bp.quizservice.repository;

import cz.cvut.fel.bp.quizservice.model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    Optional<QuizSession> findByLobbyPin(String lobbyPin);


    boolean existsByLobbyPin(String lobbyPin);
}