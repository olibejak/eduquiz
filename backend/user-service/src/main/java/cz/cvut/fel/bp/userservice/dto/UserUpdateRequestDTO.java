package cz.cvut.fel.bp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

/**
 * DTO for updating user information.
 * @param username updated username - empty when no change
 * @param email updated email - empty when no change
 */
@Builder
public record UserUpdateRequestDTO(

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @NotBlank
    String username,

    @Email(message = "Invalid email format")
    @NotBlank
    String email
) {}
