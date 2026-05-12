package cz.cvut.fel.bp.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing a user's quiz history record.
 * Stores information about each quiz session the user has participated in,
 * including the quiz session ID, when it was played, the user's position, and score.
 */



@Entity
@Table(name = "user_quiz_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserQuizResult extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_history_id", nullable = false)
    private QuizHistory quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer position;
}
