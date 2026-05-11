package cz.cvut.fel.bp.flashcardsservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the progress of a user on a specific flashcard question.
 * It tracks when the user last answered the question, their rating of the question,
 * and when they should review it next based on spaced repetition principles.
 */
@Entity
@Table(
        name = "flashcards_progress",
        uniqueConstraints = {
                // Info: Ensure that each user can have only one progress record per question
                @UniqueConstraint(columnNames = {"user_id", "question_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flashcards_progress_seq_gen")
    @SequenceGenerator(
            name = "flashcards_progress_seq_gen",
            sequenceName = "flashcards_progress_seq",
            allocationSize = 30
    )
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "deck_id", nullable = false)
    private Long deckId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Builder.Default
    @Column(name = "last_answered_at", nullable = false)
    private LocalDateTime lastAnsweredAt = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "user_rating", nullable = false)
    private FlashcardRating userRating = null;

    @Builder.Default
    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 0;
}
