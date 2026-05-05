package cz.cvut.fel.bp.quizservice.service.event;

import cz.cvut.fel.bp.quizservice.model.SessionState;

public record QuizEndedEvent(
        String lobbyPin
) {}
