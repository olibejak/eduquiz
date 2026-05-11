package cz.cvut.fel.bp.userservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * User quiz history response DTO, used to transfer quiz history data to the client.
 * @param id id of the user quiz history
 * @param quizSessionId id of quiz session
 * @param playedAt date and time when the quiz was played
 * @param position position of the user in the quiz session
 * @param score score of the user in the quiz session
 */
@Builder
public record UserQuizHistoryResponseDTO(
        Long id,
        Long quizSessionId,
        LocalDateTime playedAt,
        Integer position,
        Integer score
) {}
