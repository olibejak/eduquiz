package cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Strategy for mapping standard Answer entities to AnswerResponseDTOs.
 * This strategy will be used when the Answer type is not ChoiceAnswer or MatchingAnswer.
 */
@Component
public class StandardAnswerMappingStrategy implements AnswerMappingStrategy {

    private static final AnswerType SUPPORTED_TYPE = AnswerType.STANDARD;
    private static final Class<Answer> ANSWER_CLASS = Answer.class;

    @Override
    public boolean supports(@NonNull AnswerType type) {
        return type ==  SUPPORTED_TYPE;
    }

    @Override
    public AnswerResponseDTO map(@NonNull Answer answer) {
        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .text(answer.getText())
                .type(SUPPORTED_TYPE)
                .payload(null)
                .build();
    }

    @Override
    public Answer map(@NonNull AnswerRequestDTO request) {
        return Answer.builder()
                .text(request.text())
                .answerType(SUPPORTED_TYPE)
                .build();
    }

    @Override
    public Answer map(@NonNull AnswerRequestDTO request, @NonNull Answer answer) {
        Answer standardAnswer = this.convertAnswerFromDB(answer, ANSWER_CLASS);
        return standardAnswer.toBuilder()
                .text(request.text())
                .build();
    }
}
