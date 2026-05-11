package cz.cvut.fel.bp.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for updating the user's password.
 * @param currentPassword current password of the user before change
 * @param newPassword new user's password
 */
@Builder
public record PasswordChangeRequestDTO(
        @NotBlank(message = "Current password mustn't be empty")
        String currentPassword,

        @NotBlank(message = "New password mustn't be empty")
        @Size(min = 6, message = "New password must be at least 6 characters long")
        String newPassword
) {}
