package cz.cvut.fel.bp.userservice.dto.quiz;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserHistoryItemDTO(
        Long sessionId,
        List<String> deckTitles,
        LocalDateTime playedAt,
        Integer myScore,
        Integer myPosition
) {}
