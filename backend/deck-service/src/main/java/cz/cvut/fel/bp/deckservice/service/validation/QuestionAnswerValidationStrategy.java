package cz.cvut.fel.bp.deckservice.service.validation;

import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;

/**
 * Strategy contract for answer validation per question type.
 */
public interface QuestionAnswerValidationStrategy {

    /**
     * Indicates which question type this strategy supports.
     * @return the supported question type
     */
    QuestionType supports();

    /**
     * Performs validation of the question's answers according to the rules of the supported question type.
     * @param question the question to validate
     */
    void validate(Question question);
}

