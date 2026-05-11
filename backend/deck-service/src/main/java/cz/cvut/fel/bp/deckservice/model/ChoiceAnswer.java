package cz.cvut.fel.bp.deckservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a choice answer for a multiple-choice question.
 * It extends the base Answer class and adds an "isCorrect" field to indicate if the choice is correct.
 * This allows for easy identification of correct answers in multiple-choice questions.
 */
@Entity
@DiscriminatorValue("CHOICE")
@Getter @Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ChoiceAnswer extends Answer {

    @Column(name = "is_correct",
        nullable = true) // Warn: Explicitly allow null values to handle cases where correctness is not specified
    private Boolean isCorrect;

}