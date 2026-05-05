package cz.cvut.fel.bp.userservice.service.fasade;

import cz.cvut.fel.bp.userservice.dto.UserInfoDTO;
import cz.cvut.fel.bp.userservice.dto.UserResponseDTO;
import cz.cvut.fel.bp.userservice.dto.UserUpdateRequestDTO;
import cz.cvut.fel.bp.userservice.security.UserPrincipal;
import cz.cvut.fel.bp.userservice.mapper.UserMapper;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;

/**
 * Facade for user-related operations.
 * This class serves as an intermediary between the controllers/clients and the service layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceFacade {

    private final UserMapper userMapper;
    private final UserService userService;

    /**
     * Updates the user entity with the new info from the DTO and calls the service to update it.
     * @param userId ID of the user that is being updated
     * @param dto DTO with the new user info (username, email)
     * @return DTO response with the updated user info
     */
    @Transactional
    public UserResponseDTO updateUserProfile(UUID userId, UserUpdateRequestDTO dto) {
        log.debug("Update profile request userId={}", userId);
        User user = userService.getUserById(userId);
        userMapper.updateUserFromDTO(dto, user);
        User updatedUser = userService.updateUserProfile(user);
        UserResponseDTO response = userMapper.userToUserResponse(updatedUser);
        log.debug("Update profile completed userId={}", userId);
        return response;
    }

    /**
     * Calls the service to delete the user by ID.
     * @param userId ID of the user that is being deleted
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.debug("Delete profile request userId={}", userId);
        userService.deleteUser(userId);
        log.debug("Delete profile completed userId={}", userId);
    }

    /**
     * Calls the service to get a slice of users whose usernames contain the given keyword,
     * then maps the result to a slice of response DTOs.
     * @param keyword Keyword to search for in usernames
     * @param pageable Pagination information (page number, size, sorting)
     * @return DTO response of the searched user
     */
    @Transactional(readOnly = true)
    public Slice<UserResponseDTO> getUserByUsername(String keyword, Pageable pageable) {
        log.debug(
                "Search users by username request keyword={}, page={}, size={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        Slice<User> userSlice = userService.getUsersByUsername(keyword, pageable);
        Slice<UserResponseDTO> responseSlice = userSlice.map(userMapper::userToUserResponse);
        log.debug(
                "Search users by username completed keyword={}, page={}, size={}, resultCount={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                responseSlice.getNumberOfElements()
        );
        return responseSlice;
    }

    /**
     * Calls the service to get a list of user IDs whose usernames contain the given keyword.
     * @param keyword Keyword to search for in usernames
     * @return List of user IDs matching the search criteria
     */
    public List<UUID> searchUserIdsByKeyword(String keyword) {
        log.debug("Search user IDs request keyword={}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            log.debug("Search user IDs skipped reason=blankKeyword");
            return Collections.emptyList();
        }
        String normalizedKeyword = keyword.trim();
        List<UUID> userIds = userService.getUserIdsByUsername(normalizedKeyword);
        log.debug("Search user IDs completed keyword={}, resultCount={}", normalizedKeyword, userIds.size());
        return userIds;
    }

    /**
     * Common logic for all get methods from UserPrincipal with different returns.
     * @param principal User principal from security context
     * @param mapper Mapper that is being used to map the principal to the desired DTO
     * @return DTO of the user mapped by the given mapper
     * @param <T> DTO type that will be returned
     */
    private <T> T getUserFromUserPrincipal(UserPrincipal principal, Function<UserPrincipal, T> mapper) {
        return mapper.apply(principal);
    }

    /**
     * Calls the service to get the user info from the UserPrincipal and maps it to UserInfoDTO (for other microservices).
     * @param principal User principal from security context
     * @return DTO of the user info mapped by the mapper
     */
    public UserInfoDTO getUserInfoFromPrincipal(UserPrincipal principal) {
        log.debug("Map principal to user info request userId={}", principal.id());
        UserInfoDTO userInfo = getUserFromUserPrincipal(principal, userMapper::userPrincipalToUserInfo);
        log.debug("Map principal to user info completed userId={}", principal.id());
        return userInfo;
    }

    /**
     * Calls the service to get the user info from the UserPrincipal and maps it to UserResponseDTO (for client).
     * @param principal User principal from security context
     * @return DTO of the user response mapped by mapper
     */
    public UserResponseDTO getUserResponseFromPrincipal(UserPrincipal principal) {
        log.debug("Map principal to user response request userId={}", principal.id());
        UserResponseDTO userResponse = getUserFromUserPrincipal(principal, userMapper::userPrincipalToUserResponse);
        log.debug("Map principal to user response completed userId={}", principal.id());
        return userResponse;
    }

    public UserPrincipal getUserPrincipalByOidcSubject(String subject) {
        log.debug("Get principal by subject request subject={}", subject);
        User user = userService.getUserByOidcSubject(subject);
        UserPrincipal principal = userMapper.userToUserPrincipal(user);
        log.debug("Get principal by subject completed userId={}", principal.id());
        return principal;
    }

    public Map<UUID, String> getUsernamesByIds(Set<UUID> userIds) {
        int requestedIdsCount = userIds == null ? 0 : userIds.size();
        log.debug("Get usernames by IDs request idCount={}", requestedIdsCount);
        if (userIds == null || userIds.isEmpty()) {
            log.debug("Get usernames by IDs skipped reason=emptyIds");
            return Map.of();
        }
        Map<UUID, String> usernames = userService.getUsernamesByIds(userIds);
        log.debug("Get usernames by IDs completed idCount={}, resultCount={}", requestedIdsCount, usernames.size());
        return usernames;
    }

    public UserInfoDTO getUserById(UUID id) {
        log.debug("Get user by ID request userId={}", id);
        User user = userService.getUserById(id);
        UserInfoDTO response = userMapper.userToUserInfo(user);
        log.debug("Get user by ID completed userId={}", id);
        return response;
    }

    /*
    Info: OAuth2 will be used
    Todo: Implement own registration and security
    /**
     * Maps the registration DTO to a User entity
     * and calls the service to register the user,
     * then maps the result back to a response DTO.
     * @param dto User registration DTO
     * @return DTO response with the registered user info
    @Transactional
    public UserResponseDTO registerUser(UserRegistrationRequestDTO dto) {
        User user = userService.registerUser(
                userMapper.fromRegistrationToEntity(dto)
        );

        // Todo: send welcome email, log registration, etc.

        return userMapper.toResponseDTO(user);
    }

    Info: OAuth2 will be used
    /**
     * Calls the service to change the user's password.
     * @param userId ID of the user that requested the password change
     * @param dto DTO that contains the current and to-be changed passwords
    @Transactional
    public void changePassword(UUID userId, PasswordChangeRequestDTO dto) {
        userService.changePassword(userId, dto.currentPassword(), dto.newPassword());
    }

    Info: Methods for getting specific user DTO
          Currently not gonna be used - the user DTOs will be obtained from the security context
    /**
     * Common logic for all get methods by OIDC subject with different returns.
     * @param oidcSubject OIDC subject of the user to find
     * @param mapper mapper that is being used
     * @return DTO of the user mapped by the given mapper
     * @param <T> DTO type that will be returned
    private <T> T getUserByOidcSubject(String oidcSubject, Function<User, T> mapper) {
        User user = userService.findByOidcSubject(oidcSubject)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return mapper.apply(user);
    }

    /**
     * Calls the service to find the user by OIDC subject and maps it to UserInfoDTO (for other microservices).
     * @param oidcSubject OIDC subject of the user to find
     * @return DTO of the user info mapped by the mapper
    public UserInfoDTO getUserInfoByOidcSubject(String oidcSubject) {
        return getUserByOidcSubject(oidcSubject, userMapper::toUserInfoDTO);
    }

    /**
     * Calls the service to find the user by OIDC subject and maps it to UserResponseDTO (for client).
     * @param oidcSubject OIDC subject of the user to find
     * @return DTO of the user response mapped by mapper
    public UserResponseDTO getUserProfileByOidcSubject(String oidcSubject) {
        return getUserByOidcSubject(oidcSubject, userMapper::toResponseDTO);
    }

    /**
     * Calls the service to find the user by OIDC subject and maps it to UserPrincipal (for security).
     * @param oidcSubject OIDC subject of the user to find
     * @return Principal of the user mapped by the mapper
    public UserPrincipal getUserPrincipalByOidcSubject(String oidcSubject) {
        return getUserByOidcSubject(oidcSubject, userMapper::toPrincipal);
    }
    */
}
