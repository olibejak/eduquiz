package cz.cvut.fel.bp.deckservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.bp.deckservice.configuration.SecurityConfig;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.deckservice.model.AnswerType;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import cz.cvut.fel.bp.deckservice.security.CustomJwtConverter;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.facade.QuestionServiceFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestionController.class)
@Import(SecurityConfig.class)
class QuestionControllerTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private QuestionServiceFacade questionServiceFacade;

    @MockitoBean
    private CustomJwtConverter customJWTConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldCreateQuestionWhenAuthenticated() throws Exception {
        QuestionRequestDTO request = validQuestionRequest();
        QuestionResponseDTO response = new QuestionResponseDTO(66L, "What is 2 + 2?", QuestionType.NUMERIC, List.of(), 30);

        when(questionServiceFacade.createQuestion(eq(10L), any(QuestionRequestDTO.class), any(UserPrincipal.class)))
                .thenReturn(response);

        mockMvc.perform(post("/deck/10/questions")
                        .with(authentication(userAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/deck/10/questions/66")))
                .andExpect(jsonPath("$.id").value(66))
                .andExpect(jsonPath("$.duration").value(30));
    }

    @Test
    void shouldUpdateQuestionWhenAuthenticated() throws Exception {
        QuestionRequestDTO request = validQuestionRequest();
        QuestionResponseDTO response = new QuestionResponseDTO(77L, "Updated", QuestionType.WRITE, List.of(), 45);

        when(questionServiceFacade.updateQuestion(eq(77L), eq(10L), any(QuestionRequestDTO.class), any(UserPrincipal.class)))
                .thenReturn(response);

        mockMvc.perform(put("/deck/10/questions/77")
                        .with(authentication(userAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.text").value("Updated"))
                .andExpect(jsonPath("$.duration").value(45));
    }

    @Test
    void shouldDeleteQuestionWhenAuthenticated() throws Exception {
        doNothing().when(questionServiceFacade).deleteQuestion(eq(88L), eq(10L), any(UserPrincipal.class));

        mockMvc.perform(delete("/deck/10/questions/88")
                        .with(authentication(userAuthentication())))
                .andExpect(status().isNoContent());

        verify(questionServiceFacade).deleteQuestion(eq(88L), eq(10L), any(UserPrincipal.class));
    }

    @Test
    void shouldReturnBadRequestWhenQuestionPayloadInvalid() throws Exception {
        String invalidPayload = """
                {
                  "text":"",
                  "questionType":null,
                  "answers":[]
                }
                """;

        mockMvc.perform(post("/deck/10/questions")
                        .with(authentication(userAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingQuestion() throws Exception {
        when(questionServiceFacade.updateQuestion(eq(404L), eq(10L), any(QuestionRequestDTO.class), any(UserPrincipal.class)))
                .thenThrow(new ResourceNotFoundException("Question", 404L));

        mockMvc.perform(put("/deck/10/questions/404")
                        .with(authentication(userAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnUnauthorizedWhenCreateQuestionWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/deck/10/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validQuestionRequest())))
                .andExpect(status().isUnauthorized());
    }

    private QuestionRequestDTO validQuestionRequest() {
        AnswerRequestDTO answer = AnswerRequestDTO.builder()
                .text("4")
                .type(AnswerType.STANDARD)
                .payload(null)
                .build();

        return new QuestionRequestDTO("What is 2 + 2?", QuestionType.NUMERIC, List.of(answer), 30);
    }

    private UsernamePasswordAuthenticationToken userAuthentication() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(TEST_USER_ID)
                .role("ROLE_USER")
                .build();
        return new UsernamePasswordAuthenticationToken(
                principal,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}


