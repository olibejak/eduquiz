package cz.cvut.fel.bp.deckservice.service.validation;

import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Validates answer combinations for question types with stricter answer rules.
 */
@Component
public class QuestionAnswerValidator {

    private final Map<QuestionType, QuestionAnswerValidationStrategy> strategyByType;

    @Autowired
    public QuestionAnswerValidator(List<QuestionAnswerValidationStrategy> strategies) {
        EnumMap<QuestionType, QuestionAnswerValidationStrategy> strategyMap = new EnumMap<>(QuestionType.class);
        for (QuestionAnswerValidationStrategy strategy : strategies) {
            QuestionType type = strategy.supports();
            if (strategyMap.containsKey(type)) {
                throw new IllegalStateException("Duplicate answer validation strategy for question type: " + type);
            }
            strategyMap.put(type, strategy);
        }
        this.strategyByType = strategyMap;
    }

    public void validateQuestionAnswers(Question question) {
        if (question == null || question.getQuestionType() == null) {
            throw new IllegalArgumentException("Question and question type must not be null");
        }

        List<Answer> answers = question.getAnswers();
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Question must contain at least one answer");
        }

        QuestionAnswerValidationStrategy strategy = strategyByType.get(question.getQuestionType());
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "No validation strategy registered for question type: " + question.getQuestionType()
            );
        }

        strategy.validate(question);
    }
}

