package cz.cvut.fel.bp.quizservice.service.event;

public record LobbyCreatedEvent(
        String lobbyPin,
        Long hostParticipantId
) {}

