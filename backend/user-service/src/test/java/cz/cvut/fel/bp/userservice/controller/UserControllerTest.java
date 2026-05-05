package cz.cvut.fel.bp.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.bp.userservice.dto.UserResponseDTO;
import cz.cvut.fel.bp.userservice.dto.UserUpdateRequestDTO;
import cz.cvut.fel.bp.userservice.model.UserRole;
import cz.cvut.fel.bp.userservice.security.UserPrincipal;
import cz.cvut.fel.bp.userservice.service.fasade.UserServiceFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserServiceFacade userServiceFacade;


    @Test
    void shouldReturnCurrentUserFromPrincipal() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .username("alice")
                .role(UserRole.USER.toString())
                .build();

        when(userServiceFacade.getUserResponseFromPrincipal(principal))
                .thenReturn(UserResponseDTO.builder()
                        .id(userId)
                        .username("alice")
                        .email("alice@example.com")
                        .role(UserRole.USER)
                        .build());

        mockMvc.perform(get("/users/me")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal,
                                "n/a",
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void shouldUpdateCurrentUserProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = UserPrincipal.builder()
                .id(userId)
                .username("before")
                .role(UserRole.USER.toString())
                .build();
        UserUpdateRequestDTO request = UserUpdateRequestDTO.builder()
                .username("after")
                .email("after@example.com")
                .build();

        when(userServiceFacade.updateUserProfile(userId, request))
                .thenReturn(UserResponseDTO.builder()
                        .id(userId)
                        .username("after")
                        .email("after@example.com")
                        .role(UserRole.USER)
                        .build());

        mockMvc.perform(patch("/users/me")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal,
                                "n/a",
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        )))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("after"));

        verify(userServiceFacade).updateUserProfile(userId, request);
    }
}

