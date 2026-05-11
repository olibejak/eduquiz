package cz.cvut.fel.bp.deckservice.mapper;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.exception.UnsupportedAnswerTypeException;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.AnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.model.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting Answer entities to AnswerResponseDTOs.
 * This mapper uses a list of strategies to handle different types of Answer entities (e.g., ChoiceAnswer, MatchingAnswer).
 * The appropriate strategy is selected based on the type of the Answer entity being mapped.
 */
@RequiredArgsConstructor
@Component
public class AnswerMapperImpl implements AnswerMapper {

    private final List<AnswerMappingStrategy> strategies;

    @Override
    public AnswerResponseDTO toResponse(Answer answer) {
        if (answer == null) {
            return null;
        }

        return strategies.stream()
                .filter(strategy -> strategy.supports(answer.getAnswerType()))
                .findFirst()
                .map(strategy -> strategy.map(answer))
                .orElseThrow(() -> new UnsupportedAnswerTypeException(answer.getAnswerType()));
    }

    @Override
    public Answer toEntity(AnswerRequestDTO request) {
        if (request == null) {
            return null;
        }

        if (request.type() == null) {
            throw new IllegalArgumentException("Request DTO must have a non-null answer type to map it to an entity.");
        }

        return strategies.stream()
                .filter(strategy -> strategy.supports(request.type()))
                .findFirst()
                .map(strategy -> strategy.map(request))
                .orElseThrow(() -> new UnsupportedAnswerTypeException(request.type()));
    }

    @Override
    public Answer updateEntityFromDTO(AnswerRequestDTO request, Answer answer) {

        if (request == null || answer == null) {
            return answer;
        }

        if (answer.getAnswerType() == null) {
            throw new IllegalArgumentException("DB entity must have a non-null answer type to update it from DTO.");
        }

        return strategies.stream()
                .filter(strategy -> strategy.supports(answer.getAnswerType()))
                .findFirst()
                .map(strategy -> strategy.map(request, answer))
                .orElseThrow(() -> new UnsupportedAnswerTypeException(answer.getAnswerType()));
    }
}
