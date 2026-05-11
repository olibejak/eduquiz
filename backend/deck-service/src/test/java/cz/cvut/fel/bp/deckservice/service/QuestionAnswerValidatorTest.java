package cz.cvut.fel.bp.deckservice.service;

import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.MatchingAnswer;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import cz.cvut.fel.bp.deckservice.service.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionAnswerValidatorTest {

    private QuestionAnswerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuestionAnswerValidator(
                List.of(
                        new MultipleChoiceQuestionValidationStrategy(),
                        new MatchingQuestionValidationStrategy(),
                        new WriteQuestionValidationStrategy(),
                        new NumericQuestionValidationStrategy()
                )
        );
    }

    @Test
    void shouldRejectMultipleChoiceWithoutCorrectAnswer() {
        Question question = Question.builder()
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .text("Select correct")
                .duration(30)
                .build();

        question.addAnswer(ChoiceAnswer.builder().text("A").isCorrect(false).build());
        question.addAnswer(ChoiceAnswer.builder().text("B").isCorrect(false).build());

        assertThatThrownBy(() -> validator.validateQuestionAnswers(question))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one correct answer");
    }

    @Test
    void shouldAcceptMultipleChoiceWithAtLeastOneCorrectAnswer() {
        Question question = Question.builder()
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .text("Select correct")
                .duration(30)
                .build();

        question.addAnswer(ChoiceAnswer.builder().text("A").isCorrect(false).build());
        question.addAnswer(ChoiceAnswer.builder().text("B").isCorrect(true).build());

        assertThatCode(() -> validator.validateQuestionAnswers(question)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMatchingWhenPairForMatchIdIsIncomplete() {
        Question question = Question.builder()
                .questionType(QuestionType.MATCHING)
                .text("Match terms")
                .duration(30)
                .build();

        question.addAnswer(MatchingAnswer.builder().text("Term A").matchId(1).associate(true).build());
        question.addAnswer(MatchingAnswer.builder().text("Def B").matchId(2).associate(false).build());

        assertThatThrownBy(() -> validator.validateQuestionAnswers(question))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incomplete pairs");
    }

    @Test
    void shouldAcceptMatchingWhenEveryMatchIdHasAssociateTrueAndFalse() {
        Question question = Question.builder()
                .questionType(QuestionType.MATCHING)
                .text("Match terms")
                .duration(30)
                .build();

        question.addAnswer(MatchingAnswer.builder().text("Term A").matchId(1).associate(true).build());
        question.addAnswer(MatchingAnswer.builder().text("Def A").matchId(1).associate(false).build());
        question.addAnswer(MatchingAnswer.builder().text("Term B").matchId(2).associate(true).build());
        question.addAnswer(MatchingAnswer.builder().text("Def B").matchId(2).associate(false).build());

        assertThatCode(() -> validator.validateQuestionAnswers(question)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWriteWithoutAnswers() {
        Question question = Question.builder()
                .questionType(QuestionType.WRITE)
                .text("Write answer")
                .duration(30)
                .build();

        assertThatThrownBy(() -> validator.validateQuestionAnswers(question))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one answer");
    }

    @Test
    void shouldAcceptWriteWithAtLeastOneAnswer() {
        Question question = Question.builder()
                .questionType(QuestionType.WRITE)
                .text("Write answer")
                .duration(30)
                .build();
        question.addAnswer(Answer.builder().text("Free text").build());

        assertThatCode(() -> validator.validateQuestionAnswers(question)).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptNumericWithExactlyOneAnswer() {
        Question question = Question.builder()
                .questionType(QuestionType.NUMERIC)
                .text("2 + 2")
                .duration(30)
                .build();
        question.addAnswer(Answer.builder().text("4").build());

        assertThatCode(() -> validator.validateQuestionAnswers(question)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNumericWithMoreThanOneAnswer() {
        Question question = Question.builder()
                .questionType(QuestionType.NUMERIC)
                .text("2 + 2")
                .duration(30)
                .build();
        question.addAnswer(Answer.builder().text("4").build());
        question.addAnswer(Answer.builder().text("5").build());

        assertThatThrownBy(() -> validator.validateQuestionAnswers(question))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly one answer");
    }
}

