package cz.cvut.fel.bp.userservice.service.fasade;

import cz.cvut.fel.bp.userservice.dto.UserResponseDTO;
import cz.cvut.fel.bp.userservice.mapper.UserMapper;
import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.service.GoogleAuthService;
import cz.cvut.fel.bp.userservice.service.UserService;
import cz.cvut.fel.bp.userservice.service.util.GoogleUserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthServiceFacade {

    private final GoogleAuthService googleAuthService;
    private final UserService userService;
    private final UserMapper userMapper;

    public UserResponseDTO registerUser(String googleToken) {
        log.debug("Register user from google token={}", googleToken);
        GoogleUserData userData = googleAuthService.extractUserDataFromGoogleToken(googleToken);
        User user = userService.createUser(
                userMapper.oidcRegistrationToUser(userData.name(), userData.email(), userData.oidcSubject())
        );
        UserResponseDTO response = userMapper.userToUserResponse(user);
        log.debug("Completed register user from google token={}", response.id());
        return response;
    }

    public UserResponseDTO loginUser(String googleToken) {
        log.debug("Login user from google token={}", googleToken);
        String oidcSubject = googleAuthService.extractSubjectFromGoogleToken(googleToken);
        User user = userService.getUserByOidcSubject(oidcSubject);
        UserResponseDTO response = userMapper.userToUserResponse(user);
        log.debug("Completed login user from google token={}", response.id());
        return response;
    }
}
