package cz.cvut.fel.bp.quizservice.dto.quiz.results;

public record ParticipantQuestionResultsDTO(
        Long participantId,
        boolean isCorrect,
        Integer currentScore
) {}
