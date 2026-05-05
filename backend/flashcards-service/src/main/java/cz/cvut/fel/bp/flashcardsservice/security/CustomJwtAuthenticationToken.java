package cz.cvut.fel.bp.flashcardsservice.security;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Custom Authentication token that holds the UserPrincipal and the original JWT token.
 * Temporarily used to pass the UserPrincipal from the CustomJwtConverter to the SecurityContext.
 */
public class CustomJwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;
    private final Jwt token;

    public CustomJwtAuthenticationToken(UserPrincipal principal, Jwt token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
}
