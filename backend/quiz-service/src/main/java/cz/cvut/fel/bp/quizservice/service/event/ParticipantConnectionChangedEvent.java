package cz.cvut.fel.bp.quizservice.service.event;

public record ParticipantConnectionChangedEvent(
        String lobbyPin,
        Long participantId,
        String nickname,
        Status status
) {
    public enum Status {
        JOINED,
        CONNECTED,
        DISCONNECTED,
        LEFT,
        KICKED
    }
}

