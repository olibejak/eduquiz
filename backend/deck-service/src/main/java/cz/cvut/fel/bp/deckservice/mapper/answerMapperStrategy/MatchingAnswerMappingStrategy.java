package cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.AnswerPayload;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.ChoiceAnswerPayload;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.MatchingAnswerPayload;
import cz.cvut.fel.bp.deckservice.exception.UnsupportedAnswerTypeException;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import cz.cvut.fel.bp.deckservice.model.MatchingAnswer;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * Strategy for mapping MatchingAnswer entities to AnswerResponseDTOs.
 * This strategy will be used when the Answer type is MatchingAnswer.
 */
@Component
public class MatchingAnswerMappingStrategy implements AnswerMappingStrategy {

    private final AnswerType SUPPORTED_TYPE= AnswerType.MATCHING;
    private static final Class<MatchingAnswer> ANSWER_CLASS = MatchingAnswer.class;

    @Override
    public boolean supports(@NonNull AnswerType type) {
        return type == SUPPORTED_TYPE;
    }

    @Override
    public AnswerResponseDTO map(@NonNull Answer answer) {
        MatchingAnswer matching = this.convertAnswerFromDB(answer, ANSWER_CLASS);
        AnswerPayload payload = buildAnswerPayload(matching);

        return AnswerResponseDTO.builder()
                .id(matching.getId())
                .text(matching.getText())
                .type(SUPPORTED_TYPE)
                .payload(payload)
                .build();
    }

    @Override
    public Answer map(@NonNull AnswerRequestDTO request) {
        MatchingAnswerPayload answerPayload = getMatchingPayloadFromRequest(request);

        return MatchingAnswer.builder()
                .text(request.text())
                .associate(answerPayload.associate())
                .matchId(answerPayload.matchId())
                .answerType(SUPPORTED_TYPE)
                .build();
    }

    @Override
    public Answer map(@NonNull AnswerRequestDTO request, @NonNull Answer answer) {
        MatchingAnswer matchingAnswer = this.convertAnswerFromDB(answer, ANSWER_CLASS);
        MatchingAnswerPayload answerPayload = getMatchingPayloadFromRequest(request);

        return matchingAnswer.toBuilder()
                .text(request.text())
                .associate(answerPayload.associate())
                .matchId(answerPayload.matchId())
                .build();
    }

    private AnswerPayload buildAnswerPayload(MatchingAnswer matching) {
        return MatchingAnswerPayload.builder()
                .associate(matching.getAssociate())
                .matchId(matching.getMatchId())
                .build();
    }

    private MatchingAnswerPayload getMatchingPayloadFromRequest(AnswerRequestDTO request) {
        if (!(request.payload() instanceof MatchingAnswerPayload payload)) {
            throw new UnsupportedAnswerTypeException(
                    "Expected payload of type MatchingAnswerPayload for answer type MATCHING, but got: "
                            + request.payload().getClass().getSimpleName()
            );
        }
        return payload;
    }
}
