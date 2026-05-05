package cz.cvut.fel.bp.deckservice.service.validation;

import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import org.springframework.stereotype.Component;

/**
 * Validation strategy for NUMERIC questions.
 */
@Component
public class NumericQuestionValidationStrategy implements QuestionAnswerValidationStrategy {

    @Override
    public QuestionType supports() {
        return QuestionType.NUMERIC;
    }

    @Override
    public void validate(Question question) {
        if (question.getAnswers().size() != 1) {
            throw new IllegalArgumentException("NUMERIC question must contain exactly one answer");
        }
    }
}

