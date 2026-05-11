package cz.cvut.fel.bp.deckservice.mapper;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.ChoiceAnswerPayload;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.MatchingAnswerPayload;
import cz.cvut.fel.bp.deckservice.exception.UnsupportedAnswerTypeException;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.ChoiceAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.MatchingAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.StandardAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.MatchingAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnswerMapperImplTest {

    private AnswerMapperImpl answerMapper;

    @BeforeEach
    void init() {
        this.answerMapper = new AnswerMapperImpl(
                List.of(
                        new ChoiceAnswerMappingStrategy(),
                        new StandardAnswerMappingStrategy(),
                        new MatchingAnswerMappingStrategy()
                )
        );
    }

    @Test
    void shouldReturnNullWhenResponseInputIsNull() {
        assertThat(answerMapper.toResponse(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenEntityInputIsNull() {
        assertThat(answerMapper.toEntity(null)).isNull();
    }

    @Test
    void shouldReturnTargetAsIsWhenUpdateRequestIsNull() {
        Answer target = Answer.builder().text("Original").answerType(AnswerType.STANDARD).build();

        Answer updated = answerMapper.updateEntityFromDTO(null, target);

        assertThat(updated).isSameAs(target);
    }

    @Test
    void shouldMapChoiceEntityToResponse() {
        ChoiceAnswer entity = ChoiceAnswer.builder()
                .id(10L)
                .answerType(AnswerType.CHOICE)
                .text("Praha")
                .isCorrect(true)
                .build();

        AnswerResponseDTO response = answerMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.text()).isEqualTo("Praha");
        assertThat(response.type()).isEqualTo(AnswerType.CHOICE);
        assertThat(response.payload()).isInstanceOf(ChoiceAnswerPayload.class);

        ChoiceAnswerPayload payload = (ChoiceAnswerPayload) response.payload();
        assertThat(payload.isCorrect()).isTrue();
    }

    @Test
    void shouldMapStandardRequestToStandardEntity() {
        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .text("Praha")
                .type(AnswerType.STANDARD)
                .payload(null)
                .build();

        Answer entity = answerMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getText()).isEqualTo("Praha");
        assertThat(entity.getAnswerType()).isEqualTo(AnswerType.STANDARD);
    }

    @Test
    void shouldMapMatchingRequestToMatchingEntity() {
        MatchingAnswerPayload payload = MatchingAnswerPayload.builder()
                .associate(true)
                .matchId(42)
                .build();

        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .text("Pair A")
                .type(AnswerType.MATCHING)
                .payload(payload)
                .build();

        Answer entity = answerMapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity).isInstanceOf(MatchingAnswer.class);
        assertThat(entity.getText()).isEqualTo("Pair A");
        assertThat(entity.getAnswerType()).isEqualTo(AnswerType.MATCHING);

        MatchingAnswer matching = (MatchingAnswer) entity;
        assertThat(matching.getAssociate()).isTrue();
        assertThat(matching.getMatchId()).isEqualTo(42);
    }

    @Test
    void shouldMapMatchingEntityToResponseWithPayload() {
        MatchingAnswer entity = MatchingAnswer.builder()
                .id(20L)
                .answerType(AnswerType.MATCHING)
                .text("Pair B")
                .associate(false)
                .matchId(7)
                .build();

        AnswerResponseDTO response = answerMapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.type()).isEqualTo(AnswerType.MATCHING);
        assertThat(response.payload()).isInstanceOf(MatchingAnswerPayload.class);

        MatchingAnswerPayload payload = (MatchingAnswerPayload) response.payload();
        assertThat(payload.associate()).isFalse();
        assertThat(payload.matchId()).isEqualTo(7);
    }

    @Test
    void shouldUpdateChoiceEntityFromRequest() {
        ChoiceAnswer existing = ChoiceAnswer.builder()
                .id(99L)
                .text("Old")
                .isCorrect(false)
                .answerType(AnswerType.CHOICE)
                .build();

        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .text("New")
                .type(AnswerType.CHOICE)
                .payload(ChoiceAnswerPayload.builder().isCorrect(true).build())
                .build();

        Answer updated = answerMapper.updateEntityFromDTO(request, existing);

        assertThat(updated).isInstanceOf(ChoiceAnswer.class);
        assertThat(updated.getId()).isEqualTo(99L);
        assertThat(updated.getText()).isEqualTo("New");
        assertThat(((ChoiceAnswer) updated).getIsCorrect()).isTrue();
    }

    @Test
    void shouldThrowWhenRequestTypeIsNull() {
        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .text("Invalid")
                .type(null)
                .payload(null)
                .build();

        assertThatThrownBy(() -> answerMapper.toEntity(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-null answer type");
    }

    @Test
    void shouldThrowWhenChoicePayloadTypeIsInvalid() {
        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .text("Invalid payload")
                .type(AnswerType.CHOICE)
                .payload(MatchingAnswerPayload.builder().associate(true).matchId(1).build())
                .build();

        assertThatThrownBy(() -> answerMapper.toEntity(request))
                .isInstanceOf(UnsupportedAnswerTypeException.class)
                .hasMessageContaining("ChoiceAnswerPayload");
    }

    @Test
    void shouldThrowWhenStrategyIsMissingForType() {
        AnswerMapperImpl mapperWithoutMatching = new AnswerMapperImpl(
                List.of(new ChoiceAnswerMappingStrategy(), new StandardAnswerMappingStrategy())
        );

        AnswerRequestDTO request = AnswerRequestDTO.builder()
                .text("Pair")
                .type(AnswerType.MATCHING)
                .payload(MatchingAnswerPayload.builder().associate(true).matchId(2).build())
                .build();

        assertThatThrownBy(() -> mapperWithoutMatching.toEntity(request))
                .isInstanceOf(UnsupportedAnswerTypeException.class)
                .hasMessageContaining("MATCHING");
    }
}
