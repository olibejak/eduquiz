package cz.cvut.fel.bp.userservice.security;

import cz.cvut.fel.bp.userservice.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomJWTConverterTest {

    @InjectMocks
    private CustomJwtConverter customJWTConverter;

    @Test
    void shouldConvertJwtIntoAuthenticationWithRoleAuthority() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = new Jwt("token", null, null, Map.of("alg", "none"), Map.of(
                "sub", "subject-42",
                "userId", userId.toString(),
                "username", "jane",
                "role", UserRole.ADMIN.toString()
        ));

        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .username("jane")
                .role(UserRole.ADMIN.toString())
                .build();

        var authentication = customJWTConverter.convert(jwt);

        assertInstanceOf(CustomJwtAuthenticationToken.class, authentication);
        assertEquals(principal, authentication.getPrincipal());
        assertEquals(1, authentication.getAuthorities().size());
        assertEquals(UserRole.ADMIN.toString(), authentication.getAuthorities().iterator().next().getAuthority());
    }
}

