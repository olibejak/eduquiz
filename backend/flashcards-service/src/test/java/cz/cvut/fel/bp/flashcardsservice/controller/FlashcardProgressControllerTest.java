package cz.cvut.fel.bp.flashcardsservice.controller;

import cz.cvut.fel.bp.flashcardsservice.dto.deck.DeckProgressStatusDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.deck.DeckProgressSummaryDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.FlashcardReviewBatchDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.FlashcardReviewDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.QuestionDTO;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import cz.cvut.fel.bp.flashcardsservice.security.UserPrincipal;
import cz.cvut.fel.bp.flashcardsservice.service.facade.FlashcardProgressFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardProgressControllerTest {

    @Mock
    private FlashcardProgressFacade flashcardProgressFacade;

    @InjectMocks
    private FlashcardProgressController controller;

    private UserPrincipal userPrincipal;
    private UUID userId;
    private Long deckId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        deckId = 1L;
        userPrincipal = UserPrincipal.builder()
                .id(userId)
                .username("testuser")
                .role("USER")
                .build();
    }

    @Test
    void testGetDueDecksDashboard_ReturnsSliceOfDeckSummaries() {
        // Given
        var pageable = PageRequest.of(0, 10);
        var deckSummary = DeckProgressSummaryDTO.builder()
                .id(deckId)
                .title("Test Deck")
                .build();
        var expectedSlice = new SliceImpl<>(List.of(deckSummary), pageable, false);

        when(flashcardProgressFacade.getDueDecksDashboard(userId, pageable))
                .thenReturn(expectedSlice);

        // When
        var result = controller.getDueDeckFoUser(userPrincipal, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getNumberOfElements());
        assertEquals("Test Deck", result.getContent().get(0).title());
        verify(flashcardProgressFacade, times(1)).getDueDecksDashboard(userId, pageable);
    }

    @Test
    void testGetDeckProgressStatus_ReturnsDeckStatus() {
        // Given
        var deckStatus = DeckProgressStatusDTO.builder()
                .dueCount(5)
                .newCount(10)
                .learnedCount(15)
                .totalCount(30)
                .build();

        when(flashcardProgressFacade.getDeckStatus(userId, deckId))
                .thenReturn(deckStatus);

        // When
        var result = controller.getDeckProgressStatus(userPrincipal, deckId);

        // Then
        assertNotNull(result);
        assertEquals(5, result.dueCount());
        assertEquals(10, result.newCount());
        assertEquals(15, result.learnedCount());
        verify(flashcardProgressFacade, times(1)).getDeckStatus(userId, deckId);
    }

    @Test
    void testSubmitFlashcardReviewBatch_SuccessfullySubmitsReviews() {
        // Given
        var reviews = List.of(
                new FlashcardReviewDTO(1L, FlashcardRating.GOOD),
                new FlashcardReviewDTO(2L, FlashcardRating.EXCELLENT)
        );
        var batch = new FlashcardReviewBatchDTO(reviews);

        // When
        controller.submitFlashcardReviewBatch(userPrincipal, deckId, batch);

        // Then
        verify(flashcardProgressFacade, times(1)).submitCardReviewBatch(userId, deckId, batch);
    }

    @Test
    void testGetStudySession_ReturnsListOfFlashcards() {
        // Given
        var flashcards = List.of(
                QuestionDTO.builder().id(1L).text("Question 1").questionType("STANDARD").answers(List.of()).duration(30).build(),
                QuestionDTO.builder().id(2L).text("Question 2").questionType("STANDARD").answers(List.of()).duration(30).build()
        );
        
        when(flashcardProgressFacade.getStudySession(userId, deckId, 20))
                .thenReturn(flashcards);

        // When
        var result = controller.getStudySession(userPrincipal, deckId, 20);

        // Then
        assertNotNull(result);
        assertEquals(ResponseEntity.ok(flashcards), result);
        assertEquals(2, result.getBody().size());
        verify(flashcardProgressFacade, times(1)).getStudySession(userId, deckId, 20);
    }

    @Test
    void testGetStudySession_WithCustomSessionSize() {
        // Given
        int customSize = 50;
        var flashcards = List.of(
                QuestionDTO.builder().id(1L).text("Q1").questionType("STANDARD").answers(List.of()).duration(30).build()
        );
        
        when(flashcardProgressFacade.getStudySession(userId, deckId, customSize))
                .thenReturn(flashcards);

        // When
        var result = controller.getStudySession(userPrincipal, deckId, customSize);

        // Then
        assertNotNull(result);
        verify(flashcardProgressFacade, times(1)).getStudySession(userId, deckId, customSize);
    }
}


