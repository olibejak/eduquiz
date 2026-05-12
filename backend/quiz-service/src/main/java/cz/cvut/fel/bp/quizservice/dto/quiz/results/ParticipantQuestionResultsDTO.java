package cz.cvut.fel.bp.quizservice.dto.quiz.results;

public record ParticipantQuestionResultsDTO(
        Long participantId,
        String nickname,
        boolean isCorrect,
        Integer currentScore
) {}
