package cz.cvut.fel.bp.quizservice.dto;

import java.util.UUID;

public record UserQuizResultsDTO(
        UUID userId,
        String nickname,
        Integer finalScore,
        Integer position
) {
}
