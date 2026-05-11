package cz.cvut.fel.bp.deckservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Question entity.
 * Each Question has text, belongs to a Deck, and has timestamps for creation and updates.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_gen")
    @SequenceGenerator(name = "question_gen", sequenceName = "question_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String text;

    // One-to-many relationship with Answer
    // Warn: Use List for ensuring the order and avoiding issues with equals/hashCode in Sets when using JPA
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Answer> answers = new ArrayList<>();

    // Many-to-one relationship with Deck
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    // Info: in seconds
    @Column(nullable = false)
    Integer duration;

    /**
     * Helper method to add an answer to the question.
     * This ensures that the bidirectional relationship is properly maintained.
     *
     * @param answer The answer to be added to the question.
     */
    public void addAnswer(Answer answer) {
        answers.add(answer);
        answer.setQuestion(this);
    }

    /**
     * Helper method to remove an answer from the question.
     * This ensures that the bidirectional relationship is properly maintained.
     *
     * @param answer The answer to be removed from the question.
     */
    public void removeAnswer(Answer answer) {
        answers.remove(answer);
        answer.setQuestion(null);
    }
}
