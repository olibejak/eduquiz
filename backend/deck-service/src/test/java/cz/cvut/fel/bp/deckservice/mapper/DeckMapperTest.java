package cz.cvut.fel.bp.deckservice.mapper;

import cz.cvut.fel.bp.deckservice.dto.deck.DeckDetailsResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckSummaryResponseDTO;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.ChoiceAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.MatchingAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.mapper.answerMapperStrategy.StandardAnswerMappingStrategy;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.DeckTagType;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeckMapperTest {

    private static final UUID AUTHOR_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private DeckMapper deckMapper;

    @BeforeEach
    void setUp() {
        AnswerMapper answerMapper = new AnswerMapperImpl(
                List.of(
                        new ChoiceAnswerMappingStrategy(),
                        new StandardAnswerMappingStrategy(),
                        new MatchingAnswerMappingStrategy()
                )
        );
        QuestionMapperImpl questionMapper = new QuestionMapperImpl();
        ReflectionTestUtils.setField(questionMapper, "answerMapper", answerMapper);

        DeckMapperImpl mapper = new DeckMapperImpl();
        ReflectionTestUtils.setField(mapper, "questionMapper", questionMapper);
        deckMapper = mapper;
    }

    @Test
    void shouldMapDeckToDetailsResponseIncludingNestedQuestionsAndAnswers() {
        Deck deck = Deck.builder()
                .id(1L)
                .title("Geography")
                .description("Capitals")
                .authorId(AUTHOR_ID)
                .visibility(VisibilityType.PUBLIC)
                .tags(Set.of(DeckTagType.GEOGRAPHY))
                .favoritedByUsers(Set.of(uuid(9), uuid(10), uuid(11)))
                .build();

        Question question = Question.builder()
                .id(21L)
                .text("Capital of Czechia?")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .duration(40)
                .build();
        question.addAnswer(ChoiceAnswer.builder()
                .id(31L)
                .text("Prague")
                .isCorrect(true)
                .answerType(AnswerType.CHOICE)
                .build());
        question.addAnswer(ChoiceAnswer.builder()
                .id(32L)
                .text("Brno")
                .isCorrect(false)
                .answerType(AnswerType.CHOICE)
                .build());
        deck.addQuestion(question);

        DeckDetailsResponseDTO response = deckMapper.deckToDetailsResponse(deck);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Geography");
        assertThat(response.authorId()).isEqualTo(AUTHOR_ID);
        assertThat(response.visibility()).isEqualTo(VisibilityType.PUBLIC);
        assertThat(response.favoritesCount()).isEqualTo(3);
        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().getFirst().duration()).isEqualTo(40);
        assertThat(response.questions().getFirst().answers()).hasSize(2);
    }

    @Test
    void shouldMapDeckToSummaryResponseWithComputedCounts() {
        Deck deck = Deck.builder()
                .id(2L)
                .title("Math")
                .description("Simple arithmetic")
                .authorId(uuid(7))
                .visibility(VisibilityType.PRIVATE)
                .tags(Set.of(DeckTagType.MATHEMATICS))
                .build();

        Question first = Question.builder().text("Q1").questionType(QuestionType.WRITE).duration(20).build();
        first.addAnswer(Answer.builder().text("A1").answerType(AnswerType.STANDARD).build());
        Question second = Question.builder().text("Q2").questionType(QuestionType.NUMERIC).duration(25).build();
        second.addAnswer(Answer.builder().text("2").answerType(AnswerType.STANDARD).build());
        deck.addQuestion(first);
        deck.addQuestion(second);

        deck.addFavorite(uuid(100));
        deck.addFavorite(uuid(101));

        DeckSummaryResponseDTO response = deckMapper.deckToSummaryResponse(deck, "Alice");

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.numberOfQuestions()).isEqualTo(2);
        assertThat(response.favoritesCount()).isEqualTo(2);
        assertThat(response.authorName()).isNull();
    }

    @Test
    void shouldMapDeckRequestToEntityAndIgnoreManagedFields() {
        DeckRequestDTO request = DeckRequestDTO.builder()
                .title("History")
                .description("Europe timeline")
                .visibility(VisibilityType.PUBLIC)
                .tags(Set.of(DeckTagType.HISTORY, DeckTagType.VERIFIED))
                .questions(List.of())
                .build();

        Deck entity = deckMapper.deckRequestToDeck(request);

        assertThat(entity.getTitle()).isEqualTo("History");
        assertThat(entity.getDescription()).isEqualTo("Europe timeline");
        assertThat(entity.getVisibility()).isEqualTo(VisibilityType.PUBLIC);
        assertThat(entity.getTags()).containsExactlyInAnyOrder(DeckTagType.HISTORY, DeckTagType.VERIFIED);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getAuthorId()).isNull();
        assertThat(entity.getQuestions()).isEmpty();
        assertThat(entity.getFavoritedByUsers()).isEmpty();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void shouldUpdateDeckFromRequestAndKeepIgnoredFields() {
        Deck deck = Deck.builder()
                .id(90L)
                .title("Old title")
                .description("Old desc")
                .authorId(uuid(777))
                .visibility(VisibilityType.PRIVATE)
                .tags(new HashSet<>(Set.of(DeckTagType.OTHER)))
                .build();
        Question originalQuestion = Question.builder().text("Q").questionType(QuestionType.WRITE).duration(15).build();
        originalQuestion.addAnswer(Answer.builder().text("A").answerType(AnswerType.STANDARD).build());
        deck.addQuestion(originalQuestion);
        deck.addFavorite(uuid(999));

        DeckRequestDTO request = DeckRequestDTO.builder()
                .title("New title")
                .description("New desc")
                .visibility(VisibilityType.PUBLIC)
                .tags(Set.of(DeckTagType.SCIENCE, DeckTagType.PROGRAMMING))
                .questions(List.of())
                .build();

        deckMapper.updateDeckFromRequest(request, deck);

        assertThat(deck.getId()).isEqualTo(90L);
        assertThat(deck.getAuthorId()).isEqualTo(uuid(777));
        assertThat(deck.getQuestions()).hasSize(1);
        assertThat(deck.getFavoritedByUsers()).containsExactly(uuid(999));

        assertThat(deck.getTitle()).isEqualTo("New title");
        assertThat(deck.getDescription()).isEqualTo("New desc");
        assertThat(deck.getVisibility()).isEqualTo(VisibilityType.PUBLIC);
        assertThat(deck.getTags()).containsExactlyInAnyOrder(DeckTagType.SCIENCE, DeckTagType.PROGRAMMING);
    }

    private UUID uuid(int suffix) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", suffix));
    }
}


