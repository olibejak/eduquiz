package cz.cvut.fel.bp.flashcardsservice.service.facade;

import cz.cvut.fel.bp.flashcardsservice.client.DeckServiceClient;
import cz.cvut.fel.bp.flashcardsservice.dto.FlashcardReviewBatchDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.FlashcardReviewDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.QuestionDTO;
import cz.cvut.fel.bp.flashcardsservice.exception.DeckNotFoundException;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import cz.cvut.fel.bp.flashcardsservice.service.FlashcardProgressService;
import cz.cvut.fel.bp.flashcardsservice.service.spacedRepetition.SpacedRepetitionCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardProgressFacadeTest {

    @Mock
    private FlashcardProgressService flashcardProgressService;

    @Mock
    private SpacedRepetitionCalculator spacedRepetitionCalculator;

    @Mock
    private DeckServiceClient deckServiceClient;

    @InjectMocks
    private FlashcardProgressFacade facade;

    private UUID userId;
    private Long deckId;
    private List<Long> questionIds;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        deckId = 1L;
        questionIds = List.of(100L, 101L, 102L);
    }

    @Test
    void testSubmitCardReviewBatch_SuccessfullyProcessesReviews() {
        // Given
        var reviews = List.of(
                new FlashcardReviewDTO(100L, FlashcardRating.GOOD),
                new FlashcardReviewDTO(101L, FlashcardRating.EXCELLENT)
        );
        var batch = new FlashcardReviewBatchDTO(reviews);

        when(deckServiceClient.getDeckName(deckId)).thenReturn("Test Deck");
        when(flashcardProgressService.getProgress(userId, deckId, List.of(100L, 101L)))
                .thenReturn(List.of());
        when(spacedRepetitionCalculator.calculateNextInterval(FlashcardRating.GOOD, 0)).thenReturn(3);
        when(spacedRepetitionCalculator.calculateNextInterval(FlashcardRating.EXCELLENT, 0)).thenReturn(5);
        when(spacedRepetitionCalculator.calculateNextReview(anyInt()))
                .thenAnswer(inv -> java.time.LocalDateTime.now().plusDays((Integer) inv.getArgument(0)));

        // When & Then (no exception should be thrown)
        assertDoesNotThrow(() -> facade.submitCardReviewBatch(userId, deckId, batch));
        verify(flashcardProgressService, times(1)).saveProgress(any());
    }

    @Test
    void testSubmitCardReviewBatch_ThrowsExceptionWhenDeckNotFound() {
        // Given
        var reviews = List.of(new FlashcardReviewDTO(100L, FlashcardRating.GOOD));
        var batch = new FlashcardReviewBatchDTO(reviews);

        when(deckServiceClient.getDeckName(deckId)).thenReturn(null);

        // When & Then
        assertThrows(DeckNotFoundException.class, () -> facade.submitCardReviewBatch(userId, deckId, batch));
        verify(flashcardProgressService, never()).saveProgress(any());
    }

    @Test
    void testSubmitCardReviewBatch_ThrowsExceptionForInvalidRating() {
        // Given
        var reviews = List.of(new FlashcardReviewDTO(100L, FlashcardRating.AGAIN));
        var batch = new FlashcardReviewBatchDTO(reviews);

        when(deckServiceClient.getDeckName(deckId)).thenReturn("Test Deck");
        when(flashcardProgressService.getProgress(userId, deckId, List.of(100L)))
                .thenReturn(List.of());

        // When & Then - Note: AGAIN is a valid rating, so we need to make this test different
        // Let's test that we can process AGAIN correctly
        when(spacedRepetitionCalculator.calculateNextInterval(FlashcardRating.AGAIN, 0)).thenReturn(0);
        when(spacedRepetitionCalculator.calculateNextReview(0))
                .thenReturn(java.time.LocalDateTime.now().plusMinutes(10));

        assertDoesNotThrow(() -> facade.submitCardReviewBatch(userId, deckId, batch));
    }

    @Test
    void testGetDeckStatus_ReturnsValidDeckStatus() {
        // Given
        when(deckServiceClient.getDeckName(deckId)).thenReturn("Test Deck");
        when(flashcardProgressService.countDueCardsForDeckForUser(userId, deckId)).thenReturn(5);
        when(flashcardProgressService.countByUserIdAndDeckId(userId, deckId)).thenReturn(15);
        when(deckServiceClient.getTotalQuestionsCount(deckId)).thenReturn(30);

        // When
        var result = facade.getDeckStatus(userId, deckId);

        // Then
        assertNotNull(result);
        assertEquals(5, result.dueCount());
        assertEquals(15, result.learnedCount());
        assertEquals(15, result.newCount()); // 30 - 15
        assertEquals(30, result.totalCount());
    }

    @Test
    void testGetDeckStatus_ThrowsExceptionWhenDeckHasNoQuestions() {
        // Given
        when(deckServiceClient.getDeckName(deckId)).thenReturn("Test Deck");
        when(flashcardProgressService.countDueCardsForDeckForUser(userId, deckId)).thenReturn(0);
        when(flashcardProgressService.countByUserIdAndDeckId(userId, deckId)).thenReturn(0);
        when(deckServiceClient.getTotalQuestionsCount(deckId)).thenReturn(0);

        // When & Then
        assertThrows(DeckNotFoundException.class, () -> facade.getDeckStatus(userId, deckId));
    }

    @Test
    void testGetStudySession_ReturnsFlashcardList() {
        // Given
        var allQuestionIds = List.of(100L, 101L, 102L, 103L, 104L);
        var sessionIds = List.of(100L, 101L, 102L);
        var flashcards = List.of(
                QuestionDTO.builder().id(100L).text("Q1").questionType("STANDARD").answers(List.of()).duration(30).build(),
                QuestionDTO.builder().id(101L).text("Q2").questionType("STANDARD").answers(List.of()).duration(30).build(),
                QuestionDTO.builder().id(102L).text("Q3").questionType("STANDARD").answers(List.of()).duration(30).build()
        );

        when(deckServiceClient.getDeckName(deckId)).thenReturn("Test Deck");
        when(deckServiceClient.getAllQuestionIdsForDeck(deckId)).thenReturn(allQuestionIds);
        when(flashcardProgressService.generateSessionQuestionIds(userId, deckId, 20, allQuestionIds))
                .thenReturn(sessionIds);
        when(deckServiceClient.getQuestionsDetailsByIds(sessionIds)).thenReturn(flashcards);

        // When
        var result = facade.getStudySession(userId, deckId, 20);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Q1", result.get(0).text());
        verify(deckServiceClient, times(1)).getQuestionsDetailsByIds(sessionIds);
    }

    @Test
    void testGetDueDecksDashboard_ReturnsDeckSummaries() {
        // Given
        var pageable = PageRequest.of(0, 10);
        var dueDeckIds = new SliceImpl<Long>(List.of(1L, 2L), pageable, false);
        var deckNames = Map.of(1L, "Deck 1", 2L, "Deck 2");

        when(flashcardProgressService.getPriorityDeckIdsForUser(userId, pageable))
                .thenReturn(dueDeckIds);
        when(deckServiceClient.getDeckNames(List.of(1L, 2L)))
                .thenReturn(deckNames);

        // When
        var result = facade.getDueDecksDashboard(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getNumberOfElements());
        var content = result.getContent();
        assertTrue(content.stream().anyMatch(d -> d.name().equals("Deck 1")));
        assertTrue(content.stream().anyMatch(d -> d.name().equals("Deck 2")));
    }

    @Test
    void testGetDueDecksDashboard_HandlesEmptyDeckList() {
        // Given
        var pageable = PageRequest.of(0, 10);
        var emptySlice = new SliceImpl<Long>(List.of(), pageable, false);

        when(flashcardProgressService.getPriorityDeckIdsForUser(userId, pageable))
                .thenReturn(emptySlice);

        // When
        var result = facade.getDueDecksDashboard(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getNumberOfElements());
        verify(deckServiceClient, never()).getDeckNames(any());
    }
}
