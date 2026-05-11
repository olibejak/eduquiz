package cz.cvut.fel.bp.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


/**
 * Entity representing a user's quiz history record.
 * Stores information about each quiz session the user has participated in,
 * including the quiz session ID, when it was played, the user's position, and score.
 */
@Entity
@Table(name = "user_quiz_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserQuizHistory extends AbstractEntity {

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Info: QuizSession reference from Quiz Service
    @Column(nullable = false)
    private Long quizSessionId;

    @Column(nullable = false)
    private LocalDateTime playedAt;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer score;
}
