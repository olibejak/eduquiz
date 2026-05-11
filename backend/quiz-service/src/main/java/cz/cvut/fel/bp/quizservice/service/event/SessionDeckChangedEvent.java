package cz.cvut.fel.bp.quizservice.service.event;

public record SessionDeckChangedEvent(
        String lobbyPin,
        Long deckId,
        ChangeType changeType
) {
    public enum ChangeType {
        ADDED,
        REMOVED
    }
}
