package cz.cvut.fel.bp.deckservice.mapper;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.answer.payload.ChoiceAnswerPayload;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.ChoiceAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.MatchingAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.StandardAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionMapperTest {

    private QuestionMapper questionMapper;

    @BeforeEach
    void setUp() {
        AnswerMapper answerMapper = new AnswerMapperImpl(
                List.of(
                        new ChoiceAnswerMappingStrategy(),
                        new StandardAnswerMappingStrategy(),
                        new MatchingAnswerMappingStrategy()
                )
        );
        QuestionMapperImpl mapper = new QuestionMapperImpl();
        ReflectionTestUtils.setField(mapper, "answerMapper", answerMapper);
        questionMapper = mapper;
    }

    @Test
    void shouldMapQuestionEntityToResponseIncludingAnswers() {
        Question question = Question.builder()
                .id(1L)
                .text("Pick correct city")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .duration(25)
                .build();
        question.addAnswer(ChoiceAnswer.builder()
                .id(11L)
                .text("Prague")
                .answerType(AnswerType.CHOICE)
                .isCorrect(true)
                .build());

        QuestionResponseDTO response = questionMapper.toResponse(question);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.text()).isEqualTo("Pick correct city");
        assertThat(response.questionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(response.duration()).isEqualTo(25);
        assertThat(response.answers()).hasSize(1);

        AnswerResponseDTO answerResponse = response.answers().getFirst();
        assertThat(answerResponse.id()).isEqualTo(11L);
        assertThat(answerResponse.type()).isEqualTo(AnswerType.CHOICE);
        assertThat(answerResponse.payload()).isInstanceOf(ChoiceAnswerPayload.class);
    }

    @Test
    void shouldMapQuestionRequestToEntityAndIgnoreManagedFields() {
        QuestionRequestDTO request = new QuestionRequestDTO(
                "What is 2 + 2?",
                QuestionType.NUMERIC,
                List.of(),
                30
        );

        Question entity = questionMapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getDeck()).isNull();
        assertThat(entity.getAnswers()).isEmpty();
        assertThat(entity.getText()).isEqualTo("What is 2 + 2?");
        assertThat(entity.getQuestionType()).isEqualTo(QuestionType.NUMERIC);
        assertThat(entity.getDuration()).isEqualTo(30);
    }

    @Test
    void shouldUpdateQuestionFromRequestAndKeepIgnoredFields() {
        Deck deck = Deck.builder().id(200L).title("Deck").build();

        Question entity = Question.builder()
                .id(33L)
                .text("Old text")
                .questionType(QuestionType.WRITE)
                .duration(15)
                .deck(deck)
                .build();
        entity.addAnswer(Answer.builder().id(71L).text("Old answer").answerType(AnswerType.STANDARD).build());

        QuestionRequestDTO request = new QuestionRequestDTO(
                "New text",
                QuestionType.MATCHING,
                List.of(),
                60
        );

        questionMapper.updateEntityFromDTO(request, entity);

        assertThat(entity.getId()).isEqualTo(33L);
        assertThat(entity.getDeck()).isSameAs(deck);
        assertThat(entity.getAnswers()).hasSize(1);
        assertThat(entity.getText()).isEqualTo("New text");
        assertThat(entity.getQuestionType()).isEqualTo(QuestionType.MATCHING);
        assertThat(entity.getDuration()).isEqualTo(60);
    }
}


