package cz.cvut.fel.bp.userservice.controller;

import cz.cvut.fel.bp.userservice.dto.UserResponseDTO;
import cz.cvut.fel.bp.userservice.dto.UserUpdateRequestDTO;
import cz.cvut.fel.bp.userservice.security.UserPrincipal;
import cz.cvut.fel.bp.userservice.service.fasade.UserServiceFacade;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * UserController handles user-related operations such as registration, profile retrieval, and profile updates.
 * It uses JWT for authentication and delegates business logic to the UserServiceFacade.
 */
@RestController
@RequestMapping("/api/users")
@CircuitBreaker(name = "userApi")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserServiceFacade userServiceFacade;

    /**
     * Retrieves the current authenticated user's profile information.
     * @param principal The UserPrincipal containing the authenticated user's details.
     * @return A UserResponseDTO containing the current user's profile information.
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("Get current user request userId={}", principal.id());
        UserResponseDTO userResponse = userServiceFacade.getUserResponseFromPrincipal(principal);
        log.debug("Get current user completed userId={}", principal.id());
        return userResponse;
    }

    /**
     * Updates the current authenticated user's profile information based on the provided UserUpdateRequestDTO.
     * @param principal The UserPrincipal containing the authenticated user's details.
     * @param request The UserUpdateRequestDTO containing the new profile information to be updated.
     * @return A UserResponseDTO containing the updated user's profile information.
     */
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/me")
    public UserResponseDTO updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UserUpdateRequestDTO request) {
        log.debug("Update profile request userId={}", principal.id());
        UserResponseDTO updatedUser = userServiceFacade.updateUserProfile(principal.id(), request);
        log.info("Update profile completed userId={}", principal.id());
        return updatedUser;
    }

    /**
     * Deletes the current authenticated user's account.
     * @param principal The UserPrincipal containing the authenticated user's details.
     * @return A ResponseEntity with no content if the deletion is successful.
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserPrincipal principal) {
        log.debug("Delete user request userId={}", principal.id());
        userServiceFacade.deleteUser(principal.id());
        log.info("Delete user completed userId={}", principal.id());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a user by their ID. This operation is restricted to users with the ADMIN role.
     * @param id The ID of the user to be deleted.
     * @param principal The UserPrincipal containing the authenticated admin's details.
     * @return A ResponseEntity with no content if the deletion is successful.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        log.debug("Delete user request by admin adminId={} userId={}",principal.id(), id);
        userServiceFacade.deleteUser(id);
        log.info("Completed delete user by admin adminId={} userId={}",principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}