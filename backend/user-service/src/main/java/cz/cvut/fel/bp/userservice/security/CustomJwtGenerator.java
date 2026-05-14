package cz.cvut.fel.bp.userservice.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Generates JWT tokens for user authentication containing user ID, username, and role.
 * Tokens are signed with HMAC SHA-256 using a secret key from application properties.
 */
@Component
public class CustomJwtGenerator {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.security.jwt.issuer}")
    private String jwtIssuer;

    /**
     * Generates a JWT containing the user identifier and role.
     * @param userId Database ID of the user
     * @param role Role of the user (e.g., "ROlE_USER", "ROLE_ADMIN")
     * @return Signed JWT as a text string
     */
    public String generateToken(UUID userId, String role, String username, String email) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtExpirationMs))
                .subject(userId.toString())
                .claim("userId", userId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("role", role)
                .build();

        // Info: Used cryptographic algorithm: HMAC SHA-256
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret.getBytes()));

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
