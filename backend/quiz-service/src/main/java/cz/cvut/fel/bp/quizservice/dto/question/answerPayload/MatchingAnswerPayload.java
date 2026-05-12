package cz.cvut.fel.bp.quizservice.dto.question.answerPayload;

public record MatchingAnswerPayload(
        Boolean associate,
        Integer matchId
) implements AnswerPayload {}
