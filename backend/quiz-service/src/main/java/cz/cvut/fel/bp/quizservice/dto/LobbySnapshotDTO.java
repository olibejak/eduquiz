package cz.cvut.fel.bp.quizservice.dto;

import cz.cvut.fel.bp.quizservice.model.SessionState;

import java.util.List;

public record LobbySnapshotDTO(
        String pin,
        SessionState currentState,
        List<ParticipantDTO> participants
) {
    public record ParticipantDTO(
            Long id,
            String nickname,
            int currentScore,
            boolean isConnected
    ) {}
}
