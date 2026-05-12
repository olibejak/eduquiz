package cz.cvut.fel.bp.userservice.dto.quiz;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuizEndedEventDTO(
        String lobbyPin,
        List<UserQuizResultDTO> results,
        LocalDateTime finishedAt,
        List<String> deckTitles
) {
    public record UserQuizResultDTO(
            UUID userId,
            String nickname,
            Integer finalScore,
            Integer position
    ) {
    }
}


