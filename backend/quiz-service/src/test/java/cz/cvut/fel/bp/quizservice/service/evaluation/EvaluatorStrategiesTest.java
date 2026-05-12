package cz.cvut.fel.bp.quizservice.service.evaluation;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.ChoiceAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.MatchingAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.choiceAnswer;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.matchingAnswer;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.question;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.standardSubmit;
import static org.assertj.core.api.Assertions.assertThat;

class EvaluatorStrategiesTest {

    private final StandardEvaluatorStrategy standardEvaluatorStrategy = new StandardEvaluatorStrategy();
    private final ChoiceEvaluatorStrategy choiceEvaluatorStrategy = new ChoiceEvaluatorStrategy();
    private final MatchingEvaluatorStrategy matchingEvaluatorStrategy = new MatchingEvaluatorStrategy();

    @Test
    void standardEvaluatorSupportsAndEvaluatesByText() {
        AnswerSubmitDTO submitDTO = standardSubmit("PIN123", 1L, 2L, "Paris");
        QuestionDTO questionDTO = question(2L, "Capital?", "STANDARD", List.of(
                cz.cvut.fel.bp.quizservice.testutil.TestFixtures.answer(11L, "Berlin", "STANDARD", null),
                cz.cvut.fel.bp.quizservice.testutil.TestFixtures.answer(12L, "Paris", "STANDARD", null)
        ));

        assertThat(standardEvaluatorStrategy.supports(submitDTO)).isTrue();
        assertThat(standardEvaluatorStrategy.evaluate(submitDTO, questionDTO)).isTrue();
    }

    @Test
    void choiceEvaluatorSupportsAndEvaluatesCorrectOption() {
        AnswerSubmitDTO submitDTO = new AnswerSubmitDTO("PIN123", 1L, 2L, "CHOICE", new ChoiceAnswerSubmitPayload("CHOICE", 12L));
        QuestionDTO questionDTO = question(2L, "Capital?", "CHOICE", List.of(
                choiceAnswer(11L, "Berlin", false),
                choiceAnswer(12L, "Paris", true)
        ));

        assertThat(choiceEvaluatorStrategy.supports(submitDTO)).isTrue();
        assertThat(choiceEvaluatorStrategy.evaluate(submitDTO, questionDTO)).isTrue();
    }

    @Test
    void choiceEvaluatorRejectsWrongOption() {
        AnswerSubmitDTO submitDTO = new AnswerSubmitDTO("PIN123", 1L, 2L, "CHOICE", new ChoiceAnswerSubmitPayload("CHOICE", 11L));
        QuestionDTO questionDTO = question(2L, "Capital?", "CHOICE", List.of(
                choiceAnswer(11L, "Berlin", false),
                choiceAnswer(12L, "Paris", true)
        ));

        assertThat(choiceEvaluatorStrategy.evaluate(submitDTO, questionDTO)).isFalse();
    }

    @Test
    void matchingEvaluatorSupportsAndEvaluatesExactMap() {
        AnswerSubmitDTO submitDTO = new AnswerSubmitDTO(
                "PIN123",
                1L,
                2L,
                "MATCHING",
                new MatchingAnswerSubmitPayload("MATCHING", Map.of(1L, 2L, 3L, 4L))
        );
        QuestionDTO questionDTO = question(2L, "Pairs?", "MATCHING", List.of(
                matchingAnswer(1L, "A", true, 2),
                matchingAnswer(2L, "B", false, 2),
                matchingAnswer(3L, "C", true, 4),
                matchingAnswer(4L, "D", false, 4)
        ));

        assertThat(matchingEvaluatorStrategy.supports(submitDTO)).isTrue();
        assertThat(matchingEvaluatorStrategy.evaluate(submitDTO, questionDTO)).isTrue();
    }

    @Test
    void matchingEvaluatorRejectsWrongPairing() {
        AnswerSubmitDTO submitDTO = new AnswerSubmitDTO(
                "PIN123",
                1L,
                2L,
                "MATCHING",
                new MatchingAnswerSubmitPayload("MATCHING", Map.of(1L, 4L, 3L, 2L))
        );
        QuestionDTO questionDTO = question(2L, "Pairs?", "MATCHING", List.of(
                matchingAnswer(1L, "A", true, 2),
                matchingAnswer(2L, "B", false, 2),
                matchingAnswer(3L, "C", true, 4),
                matchingAnswer(4L, "D", false, 4)
        ));

        assertThat(matchingEvaluatorStrategy.evaluate(submitDTO, questionDTO)).isFalse();
    }

    @Test
    void supportsReturnsFalseForMismatchedPayloadType() {
        AnswerSubmitDTO submitDTO = new AnswerSubmitDTO("PIN123", 1L, 2L, "STANDARD", new ChoiceAnswerSubmitPayload("CHOICE", 12L));

        assertThat(choiceEvaluatorStrategy.supports(submitDTO)).isFalse();
        assertThat(matchingEvaluatorStrategy.supports(submitDTO)).isFalse();
        assertThat(standardEvaluatorStrategy.supports(submitDTO)).isFalse();
    }
}


