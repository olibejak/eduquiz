package cz.cvut.fel.bp.deckservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Base class for different types of answers.
 * Uses single table inheritance to store all answer types in one table.
 * The specific type of answer is determined by the "answer_type" discriminator column.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "answer_type",
        discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("STANDARD")
@Getter @Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "answer_gen")
    @SequenceGenerator(name = "answer_gen", sequenceName = "answer_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, insertable = false, updatable = false)
    private AnswerType answerType;
}
