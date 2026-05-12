package cz.cvut.fel.bp.quizservice.dto;

import cz.cvut.fel.bp.quizservice.model.ParticipantRole;
import cz.cvut.fel.bp.quizservice.model.SessionState;

import java.util.List;

public record LobbySnapshotDTO(
        String pin,
        SessionState currentState,
        List<ParticipantDTO> participants,
        List<DeckDTO> decks
) {

    public record ParticipantDTO(
            Long id,
            String nickname,
            int currentScore,
            ParticipantRole participantRole,
            boolean isConnected
    ) {}

    public record DeckDTO(
            Long deckId,
            String title
    ) {}
}
