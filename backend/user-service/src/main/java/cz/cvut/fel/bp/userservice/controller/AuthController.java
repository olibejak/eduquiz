package cz.cvut.fel.bp.userservice.controller;

import cz.cvut.fel.bp.userservice.dto.GoogleTokenRequestDTO;
import cz.cvut.fel.bp.userservice.dto.UserResponseDTO;
import cz.cvut.fel.bp.userservice.security.CustomJwtGenerator;
import cz.cvut.fel.bp.userservice.security.UserPrincipal;
import cz.cvut.fel.bp.userservice.service.fasade.GoogleAuthServiceFacade;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication using Google ID tokens.
 * Provides endpoints for login, registration, and logout, and issues
 * application JWTs via an HTTP-only cookie.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@CircuitBreaker(name = "authApi")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleAuthServiceFacade googleAuthServiceFacade;
    private final CustomJwtGenerator jwtGenerator;

    /**
     * Handles user login using a Google token. It validates the token, retrieves user information, and generates
     * @param request The GoogleTokenRequestDTO containing the Google token for authentication.
     * @return a JWT for authentication.
     */
    @PostMapping("/login/google")
    public ResponseEntity<?> login(@RequestBody GoogleTokenRequestDTO request) {
        String googleToken = request.googleToken();
        log.debug("Login user request googleIdToken={}", googleToken);
        UserResponseDTO userResponse = googleAuthServiceFacade.loginUser(request.googleToken());
        log.info("Login user completed userId={}", userResponse.id());
        return buildJwtResponse(userResponse, HttpStatus.OK);
    }

    /**
     * Handles user registration using a Google token.
     * @param request The GoogleTokenRequestDTO containing the Google token for registration.
     * @return a JWT in the response cookie
     */
    @PostMapping("/register/google")
    public ResponseEntity<?> register(@RequestBody GoogleTokenRequestDTO request) {
        String googleToken = request.googleToken();
        log.debug("Register user request googleIdToken={}", googleToken);
        UserResponseDTO registeredUser = googleAuthServiceFacade.registerUser(request.googleToken());
        log.info("Create user completed userId={}", registeredUser.id());
        return buildJwtResponse(registeredUser, HttpStatus.CREATED);
    }

    /**
     * Handles user logout by clearing the JWT cookie.
     * @return A ResponseEntity with a success message and a cleared JWT cookie.
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserPrincipal user){
        log.debug("Logout user request userId={}", user.id());
        return ResponseEntity.ok()
                .header("Set-Cookie", "jwt_token=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax")
                .body(null);
    }

    private ResponseEntity<?> buildJwtResponse(UserResponseDTO userResponse, HttpStatus status) {
        String customJwt = jwtGenerator.generateToken(userResponse.id(), userResponse.role().toString(), userResponse.username());

        return ResponseEntity.status(status)
                .header("Set-Cookie", "jwt_token=" + customJwt + "; HttpOnly; Path=/; SameSite=None; Secure")
                .body(userResponse);
    }
}
