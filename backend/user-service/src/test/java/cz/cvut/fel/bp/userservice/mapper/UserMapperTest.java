package cz.cvut.fel.bp.userservice.mapper;

import cz.cvut.fel.bp.userservice.dto.UserUpdateRequestDTO;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.model.UserRole;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapOidcRegistrationFieldsAndIgnoreManagedFields() {
        User mapped = mapper.oidcRegistrationToUser("john", "john@example.com", "sub-1");

        assertEquals("john", mapped.getUsername());
        assertEquals("john@example.com", mapped.getEmail());
        assertEquals("sub-1", mapped.getOidcSubject());
        assertNull(mapped.getId());
        assertNull(mapped.getRole());
        assertTrue(mapped.getQuizHistories().isEmpty());
    }

    @Test
    void shouldUpdateOnlyUsernameAndEmailFromDto() {
        UUID userId = UUID.randomUUID();
        User existing = User.builder()
                .id(userId)
                .username("old")
                .email("old@example.com")
                .oidcSubject("sub-old")
                .role(UserRole.ADMIN)
                .build();

        mapper.updateUserFromDTO(UserUpdateRequestDTO.builder()
                .username("new")
                .email("new@example.com")
                .build(), existing);

        assertEquals("new", existing.getUsername());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals(userId, existing.getId());
        assertEquals("sub-old", existing.getOidcSubject());
        assertEquals(UserRole.ADMIN, existing.getRole());
    }
}

