package cz.cvut.fel.bp.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.bp.userservice.configuration.SecurityConfig;
import cz.cvut.fel.bp.userservice.dto.UserInfoDTO;
import cz.cvut.fel.bp.userservice.model.UserRole;
import cz.cvut.fel.bp.userservice.service.fasade.UserServiceFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalUserController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class InternalUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserServiceFacade userServiceFacade;

    @Test
    void shouldReturnUserInfoById() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userServiceFacade.getUserById(userId))
                .thenReturn(UserInfoDTO.builder().id(userId).username("john").role(UserRole.USER.toString()).build());

        mockMvc.perform(get("/internal/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void shouldReturnIdsByKeyword() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        when(userServiceFacade.searchUserIdsByKeyword("jo")).thenReturn(List.of(firstId, secondId));

        mockMvc.perform(get("/internal/users/search/id").param("keyword", "jo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(firstId.toString()))
                .andExpect(jsonPath("$[1]").value(secondId.toString()));
    }

    @Test
    void shouldReturnUsernamesByIds() throws Exception {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Set<UUID> ids = Set.of(firstId, secondId);
        when(userServiceFacade.getUsernamesByIds(ids)).thenReturn(Map.of(firstId, "a", secondId, "b"));

        mockMvc.perform(get("/internal/users/search/names")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + firstId + "']").value("a"))
                .andExpect(jsonPath("$['" + secondId + "']").value("b"));
    }
}

