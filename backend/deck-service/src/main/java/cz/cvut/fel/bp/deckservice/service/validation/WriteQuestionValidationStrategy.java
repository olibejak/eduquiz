package cz.cvut.fel.bp.deckservice.service.validation;

import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import org.springframework.stereotype.Component;

/**
 * Validation strategy for WRITE questions.
 */
@Component
public class WriteQuestionValidationStrategy implements QuestionAnswerValidationStrategy {

    @Override
    public QuestionType supports() {
        return QuestionType.WRITE;
    }

    @Override
    public void validate(Question question) {
        // No additional type-specific checks for WRITE questions.
    }
}

