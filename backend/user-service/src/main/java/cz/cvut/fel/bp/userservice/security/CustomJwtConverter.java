package cz.cvut.fel.bp.userservice.security;

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
@Component
public class CustomJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        String role = jwt.getClaimAsString("role");
        String username = jwt.getClaimAsString("username");
        String email = jwt.getClaimAsString("email");

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .username(username)
                .email(email)
                .role(role)
                .build();

        return new CustomJwtAuthenticationToken(principal, jwt, Collections.singletonList(authority));
    }
}
