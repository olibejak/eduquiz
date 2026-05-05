package cz.cvut.fel.bp.quizservice.dto.join;

public record CreateLobbyResponseDTO(
        String lobbyPin,
        Long hostParticipantId,
        String hostToken
) {}
