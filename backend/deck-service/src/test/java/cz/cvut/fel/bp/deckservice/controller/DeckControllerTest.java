package cz.cvut.fel.bp.deckservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.fel.bp.deckservice.configuration.SecurityConfig;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckDetailsResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckRequestDTO;
import cz.cvut.fel.bp.deckservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.deckservice.security.CustomJwtConverter;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.facade.DeckServiceFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeckController.class)
@Import(SecurityConfig.class)
class DeckControllerTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DeckServiceFacade deckServiceFacade;

    @MockitoBean
    private CustomJwtConverter customJWTConverter;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldGetDeckById() throws Exception {
        DeckDetailsResponseDTO response = DeckDetailsResponseDTO.builder()
                .id(12L)
                .title("Deck title")
                .authorId(TEST_USER_ID)
                .build();

        when(deckServiceFacade.getDeckById(12L)).thenReturn(response);

        mockMvc.perform(get("/api/decks/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.title").value("Deck title"))
                .andExpect(jsonPath("$.authorId").value(TEST_USER_ID.toString()));
    }

    @Test
    void shouldCreateDeckWhenAuthenticated() throws Exception {
        DeckRequestDTO request = DeckRequestDTO.builder()
                .title("Physics")
                .description("Basic physics")
                .visibility(cz.cvut.fel.bp.deckservice.model.VisibilityType.PUBLIC)
                .tags(Set.of(cz.cvut.fel.bp.deckservice.model.DeckTagType.SCIENCE))
                .questions(List.of())
                .build();

        DeckDetailsResponseDTO response = DeckDetailsResponseDTO.builder()
                .id(44L)
                .title("Physics")
                .authorId(TEST_USER_ID)
                .build();

        when(deckServiceFacade.createDeck(any(DeckRequestDTO.class), any(UserPrincipal.class))).thenReturn(response);

        mockMvc.perform(post("/api/decks")
                        .with(authentication(userAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/decks/44")))
                .andExpect(jsonPath("$.id").value(44));
    }

    @Test
    void shouldReturnBadRequestWhenCreateDeckPayloadInvalid() throws Exception {
        String invalidPayload = """
                {
                  "title":"",
                  "description":"desc",
                  "visibility":null,
                  "tags":null,
                  "questions":[]
                }
                """;

        mockMvc.perform(post("/api/decks")
                        .with(authentication(userAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenDeckMissing() throws Exception {
        when(deckServiceFacade.getDeckById(404L)).thenThrow(new ResourceNotFoundException("Deck", 404L));

        mockMvc.perform(get("/api/decks/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldDeleteDeckWhenAuthenticated() throws Exception {
        doNothing().when(deckServiceFacade).deleteDeck(eq(77L), any(UserPrincipal.class));

        mockMvc.perform(delete("/api/decks/77")
                        .with(authentication(userAuthentication())))
                .andExpect(status().isNoContent());

        verify(deckServiceFacade).deleteDeck(eq(77L), any(UserPrincipal.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenDeleteDeckWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/decks/9"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldSearchDecksByTitle() throws Exception {
        var summary = cz.cvut.fel.bp.deckservice.dto.deck.DeckSummaryResponseDTO.builder()
                .id(5L)
                .title("Java")
                .favoritesCount(1)
                .numberOfQuestions(2)
                .build();

        when(deckServiceFacade.getDecksByTitle(eq("java"), any()))
                .thenReturn(new SliceImpl<>(List.of(summary), PageRequest.of(0, 10), false));

        mockMvc.perform(get("/api/decks/search").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].title").value("Java"));
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


