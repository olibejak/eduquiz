package cz.cvut.fel.bp.userservice.service.fasade;

import cz.cvut.fel.bp.userservice.dto.UserResponseDTO;
import cz.cvut.fel.bp.userservice.dto.UserUpdateRequestDTO;
import cz.cvut.fel.bp.userservice.mapper.UserMapper;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.model.UserRole;
import cz.cvut.fel.bp.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceFacadeTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserServiceFacade userServiceFacade;

    @Test
    void shouldReturnEmptyListForBlankKeyword() {
        List<UUID> result = userServiceFacade.searchUserIdsByKeyword("   ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(userService);
    }

    @Test
    void shouldUpdateUserProfileUsingMapperAndService() {
        UUID userId = UUID.randomUUID();
        UserUpdateRequestDTO request = UserUpdateRequestDTO.builder()
                .username("new-name")
                .email("new@example.com")
                .build();
        User user = User.builder()
                .id(userId)
                .username("old-name")
                .email("old@example.com")
                .oidcSubject("sub-7")
                .role(UserRole.USER)
                .build();
        UserResponseDTO responseDTO = UserResponseDTO.builder()
                .id(userId)
                .username("new-name")
                .email("new@example.com")
                .role(UserRole.USER)
                .build();

        when(userService.getUserById(userId)).thenReturn(user);
        when(userService.updateUserProfile(user)).thenReturn(user);
        when(userMapper.userToUserResponse(user)).thenReturn(responseDTO);

        UserResponseDTO result = userServiceFacade.updateUserProfile(userId, request);

        assertEquals(userId, result.id());
        assertEquals("new-name", result.username());
        verify(userMapper).updateUserFromDTO(request, user);
        verify(userService).updateUserProfile(user);
    }

    @Test
    void shouldReturnEmptyMapForNullIds() {
        Map<UUID, String> result = userServiceFacade.getUsernamesByIds(null);

        assertEquals(Map.of(), result);
        verify(userService, never()).getUsernamesByIds(any());
    }
}

