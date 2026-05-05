package cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.AnswerPayload;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.ChoiceAnswerPayload;
import cz.cvut.fel.bp.deckservice.exception.UnsupportedAnswerTypeException;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Strategy for mapping ChoiceAnswer entities to AnswerResponseDTOs.
 * This strategy will be used when the Answer type is ChoiceAnswer.
 */
@Component
public class ChoiceAnswerMappingStrategy implements AnswerMappingStrategy {

    private final AnswerType SUPPORTED_TYPE= AnswerType.CHOICE;
    private static final Class<ChoiceAnswer> ANSWER_CLASS = ChoiceAnswer.class;

    @Override
    public boolean supports(@NonNull AnswerType type) {
        return type == SUPPORTED_TYPE;
    }

    @Override
    public AnswerResponseDTO map(@NonNull Answer answer) {
        ChoiceAnswer choice = this.convertAnswerFromDB(answer, ANSWER_CLASS);
        AnswerPayload answerPayload = buildAnswerPayload(choice);

        return AnswerResponseDTO.builder()
                .id(choice.getId())
                .text(choice.getText())
                .type(SUPPORTED_TYPE)
                .payload(answerPayload)
                .build();
    }

    @Override
    public Answer map(@NonNull AnswerRequestDTO request) {
        ChoiceAnswerPayload answerPayload = getIsCorrectFromRequest(request);

        return ChoiceAnswer.builder()
                .text(request.text())
                .isCorrect(answerPayload.isCorrect())
                .answerType(SUPPORTED_TYPE)
                .build();
    }

    @Override
    public Answer map(@NonNull AnswerRequestDTO request, @NonNull Answer answer) {
        ChoiceAnswer choiceAnswer = this.convertAnswerFromDB(answer, ANSWER_CLASS);
        ChoiceAnswerPayload answerPayload = getIsCorrectFromRequest(request);

        return choiceAnswer.toBuilder()
                .text(request.text())
                .isCorrect(answerPayload.isCorrect())
                .build();
    }

    /**
     * Helper method to build the ChoiceAnswerPayload for the response DTO.
     * @param choice The ChoiceAnswer entity from which to build the payload.
     * @return The ChoiceAnswerPayload containing the isCorrect field for the response DTO.
     */
    private AnswerPayload buildAnswerPayload(ChoiceAnswer choice) {
        return ChoiceAnswerPayload.builder()
                .isCorrect(choice.getIsCorrect())
                .build();
    }

    /**
     * Helper method to extract the isCorrect field from the request payload.
     * @param request The incoming request DTO containing the payload.
     * @return The correct payload type.
      * @throws UnsupportedAnswerTypeException if the payload is not of type ChoiceAnswerPayload.
     */
    private ChoiceAnswerPayload getIsCorrectFromRequest(AnswerRequestDTO request) {
        if (!(request.payload() instanceof ChoiceAnswerPayload payload)) {
            throw new UnsupportedAnswerTypeException(
                    "Expected payload of type ChoiceAnswerPayload for answer type CHOICE, but got: "
                            + request.payload().getClass().getSimpleName()
            );
        }
        return payload;
    }
}
