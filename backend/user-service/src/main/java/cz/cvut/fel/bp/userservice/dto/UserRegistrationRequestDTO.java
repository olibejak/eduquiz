package cz.cvut.fel.bp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO for user registration request.
 * @param username username of the user
 * @param email email of the user
 * @param password password of the user
 */
@Builder
public record UserRegistrationRequestDTO(
        @NotBlank(message = "Username mustn't be empty")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "E-mail mustn't empty")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password mustn't be empty")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {}
