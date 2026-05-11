package cz.cvut.fel.bp.deckservice.mapper;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.model.Answer;

/**
 * Mapper interface for converting between Answer entities and their corresponding DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
public interface AnswerMapper {

    /**
     * Converts an Answer entity to its corresponding AnswerResponseDTO.
     * @param answer the Answer entity to be converted
     * @return the corresponding AnswerResponseDTO
     */
    AnswerResponseDTO toResponse(Answer answer);

    /**
     * Converts an AnswerResponseDTO to its corresponding Answer entity.
     * @param request the AnswerResponseDTO to be converted
     * @return the corresponding Answer entity
     */
    Answer toEntity(AnswerRequestDTO request);

    /**
     * Updates an existing Answer entity with values from an AnswerResponseDTO.
     * @param request the source DTO containing updated values
     * @param answer the target entity to be updated
     */
    Answer updateEntityFromDTO(AnswerRequestDTO request, Answer answer);

}
