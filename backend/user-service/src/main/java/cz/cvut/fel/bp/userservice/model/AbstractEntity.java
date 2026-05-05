package cz.cvut.fel.bp.userservice.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for all entities in model.
 * Provides a common ID field that is automatically generated.
 */

@MappedSuperclass
@Getter @Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AbstractEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
}
