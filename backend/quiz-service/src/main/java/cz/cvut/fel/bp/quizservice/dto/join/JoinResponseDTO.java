package cz.cvut.fel.bp.quizservice.dto.join;

import lombok.Builder;

@Builder
public record JoinResponseDTO(
        Long participantId,
        String nickname,
        String quizTitle,
        String token
) {}
