package cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import lombok.NonNull;

/**
 * Strategy interface for mapping different types of Answer entities to AnswerResponseDTOs.
 * Each implementation will handle a specific type of Answer (e.g., StandardAnswer, ChoiceAnswer, MatchingAnswer).
 */
public interface AnswerMappingStrategy {

    /**
     * Determines if this strategy supports mapping the given Answer class type.
     * @param type The type of the Answer entity to be mapped.
     * @return true if this strategy can map the given Answer class type, false otherwise.
     */
    boolean supports(@NonNull AnswerType type);

    /**
     * Helper method to safely convert an Answer entity to the expected target type.
     * @param answer The Answer entity from DB to be converted.
     * @param targetType The expected target type to convert the Answer entity to.
     * @return The converted Answer entity of the expected target type.
     * @param <T> The expected target type of the Answer entity.
     * @throws IllegalArgumentException if the provided Answer cannot be cast to the expected target type or is null.
     */
    default <T extends Answer> T convertAnswerFromDB(@NonNull Answer answer, @NonNull Class<T> targetType) {
        if (!targetType.isInstance(answer)) {
            throw new IllegalArgumentException(
                    "Expected type " + targetType.getSimpleName() +
                            ", but DB sent type " + answer.getClass().getSimpleName()
            );
        }
        return targetType.cast(answer);
    }

    /**
     * Maps the given Answer entity to an AnswerResponseDTO.
     * @param answer The Answer entity to be mapped.
     *      The actual type of this entity will determine which strategy implementation is used.
     * @return An AnswerResponseDTO representing the mapped Answer entity.
     */
    AnswerResponseDTO map(@NonNull Answer answer);

    /**
     * Determines if this strategy supports mapping the given AnswerRequestDTO type.
     * @param request The AnswerRequestDTO to be mapped.
     * @return An Answer entity representing the mapped AnswerRequestDTO.
      */
    Answer map(@NonNull AnswerRequestDTO request);

    /**
     * Maps the given AnswerRequestDTO to an existing Answer entity, updating its fields based on the data in the DTO.
     * @param request The AnswerRequestDTO containing the data to update the Answer entity with.
     * @param answer The existing Answer entity to be updated based on the data in the AnswerRequestDTO
     */
    Answer map(@NonNull AnswerRequestDTO request, @NonNull Answer answer);
}
