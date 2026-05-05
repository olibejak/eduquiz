package cz.cvut.fel.bp.quizservice.service.event;

public record QuizStartedEvent(
        String lobbyPin,
        int totalQuestions
) {}