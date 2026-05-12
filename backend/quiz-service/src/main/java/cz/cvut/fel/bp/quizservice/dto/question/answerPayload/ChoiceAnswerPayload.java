package cz.cvut.fel.bp.quizservice.dto.question.answerPayload;

public record ChoiceAnswerPayload(
        Boolean isCorrect
) implements AnswerPayload {
}

