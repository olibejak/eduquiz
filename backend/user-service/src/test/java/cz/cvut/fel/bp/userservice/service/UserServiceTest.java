package cz.cvut.fel.bp.userservice.service;

import cz.cvut.fel.bp.userservice.exception.DuplicateResourceException;
import cz.cvut.fel.bp.userservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.model.UserRole;
import cz.cvut.fel.bp.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWithSuffixWhenUsernameAlreadyExists() {
        UUID createdId = UUID.randomUUID();
        User input = User.builder()
                .username("john")
                .email("john@example.com")
                .oidcSubject("sub-1")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByOidcSubject("sub-1")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(true, true);
        when(userRepository.existsByUsername("john1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0, User.class);
            saved.setId(createdId);
            return saved;
        });

        User created = userService.createUser(input);

        assertEquals(createdId, created.getId());
        assertEquals("john1", created.getUsername());
        assertEquals(UserRole.USER, created.getRole());
        verify(userRepository).save(input);
    }

    @Test
    void shouldThrowWhenUserByIdIsMissing() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(missingId));
    }

    @Test
    void shouldThrowWhenUpdatingProfileWithDuplicateEmail() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("john")
                .email("john@example.com")
                .oidcSubject("sub-9")
                .role(UserRole.USER)
                .build();

        when(userRepository.existsByUsernameAndIdNot("john", userId)).thenReturn(false);
        when(userRepository.existsByEmailAndIdNot("john@example.com", userId)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.updateUserProfile(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnUsernamesMappedById() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        User first = User.builder().id(firstId).username("alice").oidcSubject("sub-a").role(UserRole.USER).build();
        User second = User.builder().id(secondId).username("bob").oidcSubject("sub-b").role(UserRole.USER).build();

        when(userRepository.findAllById(Set.of(firstId, secondId))).thenReturn(List.of(first, second));

        Map<UUID, String> usernames = userService.getUsernamesByIds(Set.of(firstId, secondId));

        assertEquals(2, usernames.size());
        assertEquals("alice", usernames.get(firstId));
        assertEquals("bob", usernames.get(secondId));
    }
}

