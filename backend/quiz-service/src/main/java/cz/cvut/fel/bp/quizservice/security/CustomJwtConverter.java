package cz.cvut.fel.bp.quizservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

/**
 * Custom converter that takes a JWT token and converts it into an Authentication object.
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class CustomJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        try {
            UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
            String role = jwt.getClaimAsString("role");
            String username = jwt.getClaimAsString("username");

            log.debug("action=convertJwt userId={} role={} username={}", userId, role, username);

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

            UserPrincipal principal = UserPrincipal.builder()
                    .id(userId)
                    .username(username)
                    .role(role)
                    .build();

            return new CustomJwtAuthenticationToken(principal, jwt, Collections.singletonList(authority));
        } catch (RuntimeException exception) {
            log.warn("action=convertJwt reason=invalidJwtClaims subject={}", jwt.getSubject());
            throw exception;
        }
    }
}

