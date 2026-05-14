package cz.cvut.fel.bp.userservice.controller;

import cz.cvut.fel.bp.userservice.security.CustomJwtGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/test")
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class TestAuthController {

    private final CustomJwtGenerator jwtGenerator;

    @PostMapping("/mock-login")
    public ResponseEntity<?> mockLogin() {
        String customJwt = jwtGenerator.generateToken(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                "ROLE_USER",
                "TestUser",
                "test@example.com"
        );

        return ResponseEntity.ok()
                .header("Set-Cookie", "jwt_token=" + customJwt + "; HttpOnly; Path=/; SameSite=Lax")
                .body(Map.of("message", "Mock login successful"));
    }
}