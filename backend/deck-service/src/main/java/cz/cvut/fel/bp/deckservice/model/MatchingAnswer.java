package cz.cvut.fel.bp.deckservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a matching answer for a matching question.
 * It extends the base Answer class and adds fields:
 *      "associate" field indicates which side of the matching question this answer belongs to.
 *      "matchId" field is used to group answers that belong together in the matching question.
 */
@Entity
@DiscriminatorValue("MATCHING")
@Getter @Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MatchingAnswer extends Answer {

    @Column(name = "associate",
        nullable = true) // Warn: Explicitly allow null values to handle cases where association is not specified
    private Boolean associate;

    @Column(name = "match_id",
        nullable = true) // Warn: Explicitly allow null values to handle cases where match ID is not specified
    private Integer matchId;

}