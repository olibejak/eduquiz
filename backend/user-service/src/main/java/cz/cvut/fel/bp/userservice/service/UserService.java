package cz.cvut.fel.bp.userservice.service;

import cz.cvut.fel.bp.userservice.exception.DuplicateResourceException;
import cz.cvut.fel.bp.userservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.model.UserRole;
import cz.cvut.fel.bp.userservice.repository.UserRepository;
import cz.cvut.fel.bp.userservice.service.event.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Finds a user by given ID.
     * @param id ID of the searched user
     * @return User with the given ID
     * @throws ResourceNotFoundException if user with the given ID does not exist
     */
    public User getUserById(UUID id) {
        log.debug("Get user by ID request userId={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        log.debug("Get user by ID completed userId={}", id);
        return user;
    }

    /**
     * Create method that is used when no credentials are given with OIDC subject.
     * @param oidcSubject OIDC subject of the user
     * @return Created user with generated username and OIDC subject
     */
    public User createUser(String oidcSubject) {
        log.debug("Create user with generated username request subject={}", oidcSubject);
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        User createdUser = saveUser(
                User.builder()
                        .username("User_" + randomString + oidcSubject.substring(0, 2))
                        .role(UserRole.USER)
                        .oidcSubject(oidcSubject)
                        .build()
        );
        log.info("Create user completed userId={}", createdUser.getId());
        return createdUser;
    }

    /**
     * Create method that is used when credentials are given.
     * Checks for duplicate username, email and OIDC subject and throws an exception if any of them already exists.
     * Edits the username if it already exists by appending a numeric suffix until a unique username is found.
     * Sets the user role to USER by default.
     * @param user User object containing the credentials and other information of the user to be created
     * @return Created user with the given credentials and information
     */
    public User createUser(User user) {
        log.debug("Create user request subject={}", user.getOidcSubject());
        existsByEmail(user.getEmail());
        existsByOidcSubject(user.getOidcSubject());
        try {
            existsByUsername(user.getUsername());
        } catch (DuplicateResourceException e) {
            log.debug("Create user username collision detected");
            user.setUsername(createUniqueUsername(user.getUsername()));
        }
        user.setRole(UserRole.USER);
        User createdUser = saveUser(user);
        log.info("Create user completed userId={}", createdUser.getId());
        return createdUser;
    }

    /**
     * Updates the role of the user with the given ID to the new role.
     * @param userId ID of the user whose role is being updated
     * @param newRole New role to be set for the user
     * @return Updated user with the new role
     */
    public User updateUserRole(UUID userId, UserRole newRole) {
        log.debug("Update user role request userId={}, userRole={}", userId, newRole);
        User user = getUserById(userId);
        user.setRole(newRole);
        User updatedUser = saveUser(user);
        log.info("Update user role completed userId={}, userRole={}", userId, updatedUser.getRole());
        return updatedUser;
    }

    /*
    Info: OAuth2 will be used
    Todo: Implement own registration and security
    public User registerUser(User user) {
        existsByUsername(user.getUsername());
        existsByEmail(user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);

        return saveUser(user);
    }
     */

    /**
     * Updates the profile of the user.
     * Checks if the new username and email are not already taken by other users.
     * @param user User object containing the updated information of the user.
     * @return Updated user with the new information.
     */
    public User updateUserProfile(User user) {
        log.debug("Update profile request userId={}", user.getId());
        existsByUsername(user.getId(), user.getUsername());
        existsByEmail(user.getId(), user.getEmail());
        existsByOidcSubject(user.getId(), user.getOidcSubject());
        User updatedUser = saveUser(user);
        log.info("Update profile completed userId={}", updatedUser.getId());
        return updatedUser;
    }

    /**
     * Saves the user entity.
     * @param user User entity to be stored in DB
     * @return Saved user entity with generated ID and other DB-generated fields
     */
    private User saveUser(User user) {
        User savedUser = userRepository.save(user);
        log.debug("Persist user entity completed userId={}", savedUser.getId());
        return savedUser;
    }

    /**
     * Deletes the user with the given ID.
     * @param userId ID of the user to be deleted
     */
    public void deleteUser(UUID userId) {
        log.debug("Delete user request userId={}", userId);
        User user = getUserById(userId);
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
        log.info("Delete user completed userId={}", userId);
    }

    /*
    Info: OAuth2 will be used
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
     */

    /**
     * Returns a slice of users whose usernames contain the given keyword, ignoring case.
     * @param keyword Keyword to search for in usernames
     * @param pageable Pagination information (page number, size, sorting)
     * @return Slice of users whose usernames contain the given keyword
     */
    public Slice<User> getUsersByUsername(String keyword, Pageable pageable) {
        log.debug(
                "Search users by username request keyword={}, page={}, size={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
        Slice<User> users = userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
        log.debug(
                "Search users by username completed keyword={}, page={}, size={}, resultCount={}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                users.getNumberOfElements()
        );
        return users;
    }


    public List<UUID> getUserIdsByUsername(String keyword) {
        log.debug("Search user IDs by username request keyword={}", keyword);
        List<UUID> userIds = userRepository.findUserIdsByUsernameKeyword(keyword);
        log.debug("Search user IDs by username completed keyword={}, resultCount={}", keyword, userIds.size());
        return userIds;
    }

    /**
     * Finds a user by the given OIDC subject.
     * @param subject OIDC subject to search for
     * @return User with the given OIDC subject
     * @throws ResourceNotFoundException if a user with the given OIDC subject does not exist
     */
    public User getUserByOidcSubject(String subject) {
        log.debug("Get user by subject request subject={}", subject);
        User user = userRepository.findByOidcSubject(subject)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid OIDC Subject"));
        log.debug("Get user by subject completed userId={}", user.getId());
        return user;
    }


    /**
     * Generates a unique username based on the given base string by appending a numeric suffix if necessary.
     * @param base Base string to generate the username from (e.g., name from OIDC)
     * @return Unique username that does not exist in the database
     */
    private String createUniqueUsername(String base) {
        log.debug("Generate unique username request");
        String username = base;
        int suffix = 1;

        while (userRepository.existsByUsername(username)) {
            username = base + suffix;
            suffix++;
        }

        log.debug("Generate unique username completed");
        return username;
    }

    // Todo: refactoring of exists methods
    /**
     * Checks if a user with the given username already exists.
     * @param username Username to check for existence
     * @throws DuplicateResourceException if a user with the given username already exists
     */
    private void existsByUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            log.debug("Username already exists check=failed");
            throw new DuplicateResourceException("Username'" + username + "' already exists.");
        }
    }

    /**
     * Checks if a user with the given username already exists.
     * @param username Username to check for existence
     * @throws DuplicateResourceException if a user with the given username already exists
     */
    private void existsByUsername(UUID id, String username) {
        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            log.debug("Username already exists for another user check=failed userId={}", id);
            throw new DuplicateResourceException("Username'" + username + "' already exists.");
        }
    }

    /**
     * Checks if a user with the given email already exists.
     * @param email Email to check for existence
     * @throws DuplicateResourceException if a user with the given email already exists
     */
    private void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.debug("Email already exists check=failed");
            throw new DuplicateResourceException("E-mail '" + email + "' is already being used.");
        }
    }

    /**
     * Checks if a user with the given email already exists.
     * @param email Email to check for existence
     * @throws DuplicateResourceException if a user with the given email already exists
     */
    private void existsByEmail(UUID id, String email) {
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            log.debug("Email already exists for another user check=failed userId={}", id);
            throw new DuplicateResourceException("E-mail '" + email + "' is already being used.");
        }
    }

    /**
     * Checks if a user with the given OIDC subject already exists.
     * @param oidcSubject OIDC subject to check for existence
     * @throws DuplicateResourceException if a user with the given OIDC subject already exists
     */
    private void existsByOidcSubject(String oidcSubject) {
        if (userRepository.existsByOidcSubject(oidcSubject)) {
            log.debug("OIDC subject already exists check=failed");
            throw new DuplicateResourceException("User with sent OIDC subject already exists.");
        }
    }

    /**
     * Checks if a user with the given OIDC subject already exists.
     * @param id ID of the user who is being updated (to exclude from the check)
     * @param oidcSubject OIDC subject to check for existence
     * @throws DuplicateResourceException if a user with the given OIDC subject already exists
     */
    private void existsByOidcSubject(UUID id, String oidcSubject) {
        if (userRepository.existsByOidcSubjectAndIdNot(oidcSubject, id)) {
            log.debug("OIDC subject already exists for another user check=failed userId={}", id);
            throw new DuplicateResourceException("User with sent OIDC subject already exists.");
        }
    }

    public Map<UUID, String> getUsernamesByIds(Set<UUID> userIds) {
        log.debug("Get usernames by IDs request idCount={}", userIds.size());
        Map<UUID, String> usernames = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(
                        User::getId,
                        User::getUsername,
                        (existing, replacement) -> existing
                ));
        log.debug("Get usernames by IDs completed idCount={}, resultCount={}", userIds.size(), usernames.size());
        return usernames;
    }
}
