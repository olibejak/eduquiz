package cz.cvut.fel.bp.deckservice.service.validation;

import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validates answers for MULTIPLE_CHOICE questions.
 */
@Component
@Slf4j
public class MultipleChoiceQuestionValidationStrategy implements QuestionAnswerValidationStrategy {

    @Override
    public QuestionType supports() {
        return QuestionType.MULTIPLE_CHOICE;
    }

    @Override
    public void validate(Question question) {
        boolean hasCorrectAnswer = false;

        for (Answer answer : question.getAnswers()) {
            if (!(answer instanceof ChoiceAnswer choiceAnswer)) {
                throw new IllegalArgumentException("MULTIPLE_CHOICE question can contain only CHOICE answers");
            }
            if (Boolean.TRUE.equals(choiceAnswer.getIsCorrect())) {
                hasCorrectAnswer = true;
            }
        }

        if (!hasCorrectAnswer) {
            throw new IllegalArgumentException("MULTIPLE_CHOICE question must contain at least one correct answer");
        }

        log.debug("Validated MULTIPLE_CHOICE answers questionId={}, answersCount={}",
                question.getId(), question.getAnswers().size());
    }
}

