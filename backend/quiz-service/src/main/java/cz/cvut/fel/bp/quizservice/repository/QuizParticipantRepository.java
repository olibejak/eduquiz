package cz.cvut.fel.bp.quizservice.repository;

import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizParticipantRepository extends JpaRepository<QuizParticipant, Long> {

    QuizParticipant findByToken(String token);

    // Najde konkrétního hráče v konkrétní hře (přes PIN místnosti)
    @Query("SELECT p FROM QuizParticipant p WHERE p.session.lobbyPin = :pin AND p.userId = :userId")
    Optional<QuizParticipant> findByLobbyPinAndUserId(@Param("pin") String pin, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE QuizParticipant p SET p.isCurrentCorrect = null WHERE p.session.lobbyPin = :pin")
    void resetAnswersForSession(@Param("pin") String pin);
}