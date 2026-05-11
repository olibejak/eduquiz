package cz.cvut.fel.bp.deckservice.service;

import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import cz.cvut.fel.bp.deckservice.model.VisibilityType;
import cz.cvut.fel.bp.deckservice.repository.DeckRepository;
import cz.cvut.fel.bp.deckservice.service.validation.QuestionAnswerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    private static final UUID USER_99 = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID USER_44 = UUID.fromString("00000000-0000-0000-0000-000000000044");
    private static final UUID USER_7 = UUID.fromString("00000000-0000-0000-0000-000000000007");
    private static final UUID USER_15 = UUID.fromString("00000000-0000-0000-0000-000000000015");

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private QuestionAnswerValidator questionAnswerValidator;

    private DeckService deckService;

    @BeforeEach
    void setUp() {
        deckService = new DeckService(deckRepository, questionAnswerValidator);
    }

    @Test
    void shouldValidateQuestionsAndSetAuthorBeforeDeckSave() {
        Deck deck = Deck.builder().title("Deck").build();
        Question question = Question.builder().text("Q").questionType(QuestionType.WRITE).duration(30).build();
        question.addAnswer(Answer.builder().text("Free text").build());
        deck.addQuestion(question);

        when(deckRepository.save(deck)).thenReturn(deck);

        Deck savedDeck = deckService.createDeck(USER_99, deck);

        InOrder inOrder = inOrder(questionAnswerValidator, deckRepository);
        inOrder.verify(questionAnswerValidator).validateQuestionAnswers(question);
        inOrder.verify(deckRepository).save(deck);

        assertThat(savedDeck.getAuthorId()).isEqualTo(USER_99);
        verify(deckRepository).save(deck);
    }

    @Test
    void shouldSaveDeckWithoutQuestions() {
        Deck deck = Deck.builder().title("Deck without questions").build();

        when(deckRepository.save(deck)).thenReturn(deck);

        Deck savedDeck = deckService.createDeck(USER_44, deck);

        verify(questionAnswerValidator, never()).validateQuestionAnswers(org.mockito.ArgumentMatchers.any());
        verify(deckRepository).save(deck);
        assertThat(savedDeck.getAuthorId()).isEqualTo(USER_44);
    }

    @Test
    void shouldCreateFullDeckWithQuestionsAndAnswers() {
        Deck deck = Deck.builder()
                .title("Full deck")
                .description("Deck with multiple question types")
                .visibility(VisibilityType.PUBLIC)
                .build();

        Question writeQuestion = Question.builder()
                .text("Write country capital")
                .questionType(QuestionType.WRITE)
                .duration(45)
                .build();
        writeQuestion.addAnswer(Answer.builder().text("Prague").build());

        Question multipleChoiceQuestion = Question.builder()
                .text("Select even number")
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .duration(30)
                .build();
        multipleChoiceQuestion.addAnswer(ChoiceAnswer.builder().text("3").isCorrect(false).build());
        multipleChoiceQuestion.addAnswer(ChoiceAnswer.builder().text("4").isCorrect(true).build());

        Question numericQuestion = Question.builder()
                .text("2 + 2")
                .questionType(QuestionType.NUMERIC)
                .duration(15)
                .build();
        numericQuestion.addAnswer(Answer.builder().text("4").build());

        deck.addQuestion(writeQuestion);
        deck.addQuestion(multipleChoiceQuestion);
        deck.addQuestion(numericQuestion);

        when(deckRepository.save(deck)).thenReturn(deck);

        Deck savedDeck = deckService.createDeck(USER_7, deck);

        InOrder inOrder = inOrder(questionAnswerValidator, deckRepository);
        inOrder.verify(questionAnswerValidator).validateQuestionAnswers(writeQuestion);
        inOrder.verify(questionAnswerValidator).validateQuestionAnswers(multipleChoiceQuestion);
        inOrder.verify(questionAnswerValidator).validateQuestionAnswers(numericQuestion);
        inOrder.verify(deckRepository).save(deck);

        assertThat(savedDeck.getAuthorId()).isEqualTo(USER_7);
        assertThat(savedDeck.getQuestions()).hasSize(3);
        assertThat(writeQuestion.getDeck()).isSameAs(deck);
        assertThat(multipleChoiceQuestion.getAnswers()).hasSize(2);
        assertThat(numericQuestion.getAnswers()).hasSize(1);
    }

    @Test
    void shouldSaveDeckWithoutQuestionValidationWhenDeckHasNoQuestions() {
        Deck deck = Deck.builder().title("Deck without questions").build();

        when(deckRepository.save(deck)).thenReturn(deck);

        Deck savedDeck = deckService.createDeck(USER_44, deck);

        verify(questionAnswerValidator, never()).validateQuestionAnswers(org.mockito.ArgumentMatchers.any());
        verify(deckRepository).save(deck);
        assertThat(savedDeck.getAuthorId()).isEqualTo(USER_44);
    }

    @Test
    void shouldPropagateValidationExceptionAndNotSaveDeck() {
        Deck deck = Deck.builder().title("Invalid deck").build();
        Question invalidQuestion = Question.builder().text("Q").questionType(QuestionType.WRITE).duration(30).build();
        invalidQuestion.addAnswer(Answer.builder().text("A").build());
        deck.addQuestion(invalidQuestion);

        doThrow(new IllegalArgumentException("Invalid question answers"))
                .when(questionAnswerValidator)
                .validateQuestionAnswers(invalidQuestion);

        assertThatThrownBy(() -> deckService.createDeck(USER_15, deck))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid question answers");

        verify(deckRepository, never()).save(deck);
    }

    @Test
    void shouldValidateEveryQuestionInDeckBeforeSave() {
        Deck deck = Deck.builder().title("Deck").build();

        Question first = Question.builder().text("Q1").questionType(QuestionType.WRITE).duration(20).build();
        first.addAnswer(Answer.builder().text("A1").build());
        Question second = Question.builder().text("Q2").questionType(QuestionType.NUMERIC).duration(10).build();
        second.addAnswer(Answer.builder().text("1").build());

        deck.addQuestion(first);
        deck.addQuestion(second);

        when(deckRepository.save(deck)).thenReturn(deck);

        deckService.createDeck(USER_99, deck);

        verify(questionAnswerValidator, times(1)).validateQuestionAnswers(first);
        verify(questionAnswerValidator, times(1)).validateQuestionAnswers(second);
        verify(deckRepository, times(1)).save(deck);
    }
}

