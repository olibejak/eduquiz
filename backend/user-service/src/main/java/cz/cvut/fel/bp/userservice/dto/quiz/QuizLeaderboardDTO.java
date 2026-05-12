package cz.cvut.fel.bp.userservice.dto.quiz;

import java.time.LocalDateTime;
import java.util.List;

public record QuizLeaderboardDTO(
        Long sessionId,
        List<String> deckTitles,
        LocalDateTime playedAt,
        List<LeaderboardRowDTO> players
) {
    public record LeaderboardRowDTO(
            String nickname,
            Integer score,
            Integer position
    ) {}
}