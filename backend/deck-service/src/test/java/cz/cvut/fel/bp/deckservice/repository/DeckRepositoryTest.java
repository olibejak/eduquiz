package cz.cvut.fel.bp.deckservice.repository;

import cz.cvut.fel.bp.deckservice.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the DeckRepository to ensure that entities are correctly persisted,
 * including relationships and cascading.
 */
@DataJpaTest
@ActiveProfiles("test")
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Test
    void shouldFindDecksByTitleContainingIgnoreCase() {
        Deck matching = persistDeck("Java basics", uuid(10));
        persistDeck("Python basics", uuid(20));

        Slice<Deck> result = deckRepository.findByTitleContainingIgnoreCase("JAVA", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Deck::getId).containsExactly(matching.getId());
    }

    @Test
    void shouldFindDecksByAuthorId() {
        UUID authorId = uuid(77);
        Deck expectedOne = persistDeck("A1", authorId);
        Deck expectedTwo = persistDeck("A2", authorId);
        persistDeck("Other author", uuid(88));

        Slice<Deck> result = deckRepository.findAllByAuthorId(authorId, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Deck::getId)
                .containsExactlyInAnyOrder(expectedOne.getId(), expectedTwo.getId());
    }

    @Test
    void shouldFindDecksFavoritedByUser() {
        UUID userId = uuid(555);
        Deck favorited = persistDeck("Favorited", uuid(1));
        favorited.addFavorite(userId);
        deckRepository.saveAndFlush(favorited);

        Deck notFavorited = persistDeck("Not favorited", uuid(2));
        notFavorited.addFavorite(uuid(111));
        deckRepository.saveAndFlush(notFavorited);

        Slice<Deck> result = deckRepository.findByFavoritedByUsersContains(userId, PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Deck::getId).containsExactly(favorited.getId());
    }

    @Test
    void shouldFindDecksByAuthorIdIn() {
        UUID firstAuthorId = uuid(5);
        UUID secondAuthorId = uuid(9);
        Deck first = persistDeck("Author 5", firstAuthorId);
        Deck second = persistDeck("Author 9", secondAuthorId);
        persistDeck("Author 12", uuid(12));

        Slice<Deck> result = deckRepository.findByAuthorIdIn(List.of(firstAuthorId, secondAuthorId), PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Deck::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void shouldFindDecksByTags() {
        Deck scienceDeck = persistDeck("Science", uuid(100));
        scienceDeck.addTag(DeckTagType.SCIENCE);
        deckRepository.saveAndFlush(scienceDeck);

        Deck historyDeck = persistDeck("History", uuid(101));
        historyDeck.addTag(DeckTagType.HISTORY);
        deckRepository.saveAndFlush(historyDeck);

        Slice<Deck> result = deckRepository.findByTagsIn(Set.of(DeckTagType.SCIENCE), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Deck::getId).containsExactly(scienceDeck.getId());
    }

    // ---------------------------------------------------------
    // TEST 1: Saving a Deck with Questions and Polymorphic Answers
    // ---------------------------------------------------------
    @Test
    void shouldSaveDeckWithQuestionAndChoiceAnswer() {
        // Arrange
        Deck deck = Deck.builder()
                .title("Java Basics")
                .authorId(uuid(1))
                .build();
        deck.addFavorite(uuid(5));
        deck.setVisibility(VisibilityType.PUBLIC);
        deck.addTag(DeckTagType.PROGRAMMING);

        Question question = Question.builder()
                .text("Is Java strongly typed?")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .duration(30)
                .build();

        ChoiceAnswer answer = ChoiceAnswer.builder()
                .text("Yes")
                .isCorrect(true)
                .build();

        // Setting up bidirectional relationships
        question.addAnswer(answer);
        deck.addQuestion(question);

        // Save the deck -> should cascade to questions and answers
        Deck savedDeck = deckRepository.save(deck);

        // Assert
        assertThat(savedDeck.getId()).isNotNull();
        assertThat(savedDeck.getCreatedAt()).isNotNull();

        assertThat(savedDeck.getTags()).containsExactly(DeckTagType.PROGRAMMING);
        assertThat(savedDeck.getFavoritedByUsers()).containsExactly(uuid(5));

        // Test cascading save
        assertThat(savedDeck.getQuestions()).hasSize(1);

        Question savedQuestion = savedDeck.getQuestions().getFirst();
        assertThat(savedQuestion.getId()).isNotNull();
        assertThat(savedQuestion.getAnswers()).hasSize(1);

        // Polymorphic association
        assertThat(savedQuestion.getAnswers().getFirst()).isInstanceOf(ChoiceAnswer.class);
        ChoiceAnswer savedAnswer = (ChoiceAnswer) savedQuestion.getAnswers().getFirst();
        assertThat(savedAnswer.getIsCorrect()).isTrue();
    }

    // ---------------------------------------------------------
    // TEST 2: Cascade a Orphan Removal
    // ---------------------------------------------------------
    @Test
    void shouldDeleteQuestionsAndAnswersWhenDeckIsDeleted() {

        Deck deck = Deck.builder()
                .title("Delete cascade test")
                .authorId(uuid(2))
                .visibility(VisibilityType.PUBLIC)
                .build();

        Question question = Question.builder()
                .text("Test question?")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .duration(30)
                .build();

        ChoiceAnswer answer = ChoiceAnswer.builder()
                .text("Yes")
                .isCorrect(true)
                .build();

        question.addAnswer(answer);
        deck.addQuestion(question);

        deckRepository.save(deck);
        deckRepository.flush();

        Long deckId = deck.getId();
        Long questionId = question.getId();
        Long answerId = answer.getId();

        deckRepository.deleteById(deckId);
        deckRepository.flush();

        assertThat(deckRepository.findById(deckId)).isEmpty();
        assertThat(questionRepository.findById(questionId)).isEmpty();
        assertThat(answerRepository.findById(answerId)).isEmpty();
    }

    // ---------------------------------------------------------
    // TEST 3: Saving and Retrieving Polymorphic Associations (MatchingAnswer)
    // ---------------------------------------------------------
    @Test
    void shouldSaveAndRetrieveMatchingAnswer() {

        Deck deck = Deck.builder()
                .title("Test matching answer")
                .authorId(uuid(3))
                .visibility(VisibilityType.PRIVATE)
                .build();

        Question question = Question.builder()
                .text("Match:")
                .questionType(QuestionType.MATCHING)
                .duration(30)
                .build();

        MatchingAnswer answer = MatchingAnswer.builder()
                .text("Associate A")
                .associate(true)
                .matchId(42)
                .build();

        question.addAnswer(answer);
        deck.addQuestion(question);

        Deck savedDeck = deckRepository.save(deck);

        Question savedQuestion = savedDeck.getQuestions().getFirst();
        assertThat(savedQuestion.getAnswers()).hasSize(1);

        assertThat(savedQuestion.getAnswers().getFirst()).isInstanceOf(MatchingAnswer.class);

        MatchingAnswer savedAnswer = (MatchingAnswer) savedQuestion.getAnswers().getFirst();
        assertThat(savedAnswer.getAssociate()).isTrue();
        assertThat(savedAnswer.getMatchId()).isEqualTo(42);
    }

    private Deck persistDeck(String title, UUID authorId) {
        Deck deck = Deck.builder()
                .title(title)
                .authorId(authorId)
                .visibility(VisibilityType.PUBLIC)
                .build();
        return deckRepository.saveAndFlush(deck);
    }

    private UUID uuid(int suffix) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", suffix));
    }
}
