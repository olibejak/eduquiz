package cz.cvut.fel.bp.quizservice.dto.join;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JoinRequestDTO(
        UUID userId,
        @NotBlank(message = "Nickname cannot be blank")
        @Size(min = 2, max = 15, message = "Nickname must be between 2 and 15 characters")
        String nickname,
        String deviceId
) {}
