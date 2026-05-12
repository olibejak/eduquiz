package cz.cvut.fel.bp.userservice.repository;

import cz.cvut.fel.bp.userservice.model.UserQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserQuizResultRepository extends JpaRepository<UserQuizResult, Long> {

    @Query("SELECT r FROM UserQuizResult r JOIN FETCH r.quiz q LEFT JOIN FETCH q.deckTitles WHERE r.user.id = :userId ORDER BY q.playedAt DESC")
    List<UserQuizResult> findAllByUserIdWithQuizData(@Param("userId") UUID userId);
}
