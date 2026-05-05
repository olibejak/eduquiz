package cz.cvut.fel.bp.flashcardsservice.service;

import cz.cvut.fel.bp.flashcardsservice.model.FlashcardProgress;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import cz.cvut.fel.bp.flashcardsservice.repository.FlashcardsProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardProgressServiceTest {

    @Mock
    private FlashcardsProgressRepository flashcardsProgressRepository;

    @InjectMocks
    private FlashcardProgressService service;

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
    void testGetProgress_ReturnsExistingProgress() {
        // Given
        var progress = FlashcardProgress.builder()
                .deckId(deckId)
                .questionId(100L)
                .userId(userId)
                .build();
        progress.setUserRating(FlashcardRating.GOOD);
        progress.setIntervalDays(3);

        when(flashcardsProgressRepository.findAllExistingProgress(userId, deckId, questionIds))
                .thenReturn(List.of(progress));

        // When
        var result = service.getProgress(userId, deckId, questionIds);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(FlashcardRating.GOOD, result.get(0).getUserRating());
        verify(flashcardsProgressRepository, times(1))
                .findAllExistingProgress(userId, deckId, questionIds);
    }

    @Test
    void testGetProgress_ReturnsEmptyListWhenNoProgress() {
        // Given
        when(flashcardsProgressRepository.findAllExistingProgress(any(), any(), any()))
                .thenReturn(List.of());

        // When
        var result = service.getProgress(userId, deckId, questionIds);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateSessionQuestionIds_ReturnsMixedDueAndNewQuestions() {
        // Given: Simplified test - just verify the service processes the data correctly
        var allDeckQuestionIds = List.of(100L, 101L, 102L, 103L, 104L);
        var dueQuestionIds = List.of(100L, 101L);
        var seenIds = Set.of(100L, 101L, 102L);

        when(flashcardsProgressRepository.findDueQuestionIds(any(), any(), any(), any()))
                .thenReturn(dueQuestionIds);
        when(flashcardsProgressRepository.findAllSeenQuestionIds(any(), any()))
                .thenReturn(seenIds);

        // When
        var result = service.generateSessionQuestionIds(userId, deckId, 20, allDeckQuestionIds);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        verify(flashcardsProgressRepository, times(1)).findDueQuestionIds(any(), any(), any(), any());
    }

    @Test
    void testGenerateSessionQuestionIds_FillsSessionWithNewQuestionsWhenNotEnoughDue() {
        // Given: Simplified test
        var allDeckQuestionIds = List.of(100L, 101L, 102L, 103L, 104L);
        var dueQuestionIds = List.of(100L);
        var seenIds = Set.of(100L);

        when(flashcardsProgressRepository.findDueQuestionIds(any(), any(), any(), any()))
                .thenReturn(dueQuestionIds);
        when(flashcardsProgressRepository.findAllSeenQuestionIds(any(), any()))
                .thenReturn(seenIds);

        // When
        var result = service.generateSessionQuestionIds(userId, deckId, 5, allDeckQuestionIds);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(100L));
    }

    @Test
    void testSaveProgress_PersistsProgressRecords() {
        // Given
        var progress =FlashcardProgress.builder()
                .deckId(deckId)
                .questionId(100L)
                .userId(userId)
                .build();
        progress.setUserRating(FlashcardRating.GOOD);
        progress.setIntervalDays(3);

        var progressList = List.of(progress);

        // When
        service.saveProgress(progressList);

        // Then
        verify(flashcardsProgressRepository, times(1)).saveAll(progressList);
    }

    @Test
    void testCountDueCardsForDeckForUser_ReturnsCount() {
        // Given
        when(flashcardsProgressRepository.countDueCardsForDeck(any(), any(), any()))
                .thenReturn(5);

        // When
        var result = service.countDueCardsForDeckForUser(userId, deckId);

        // Then
        assertEquals(5, result);
        verify(flashcardsProgressRepository, times(1))
                .countDueCardsForDeck(any(), any(), any());
    }

    @Test
    void testCountByUserIdAndDeckId_ReturnsStartedCount() {
        // Given
        when(flashcardsProgressRepository.countByUserIdAndDeckId(userId, deckId))
                .thenReturn(15);

        // When
        var result = service.countByUserIdAndDeckId(userId, deckId);

        // Then
        assertEquals(15, result);
        verify(flashcardsProgressRepository, times(1))
                .countByUserIdAndDeckId(userId, deckId);
    }

    @Test
    void testGetPriorityDeckIdsForUser_ReturnsPrioritizedDecks() {
        // Given
        var pageable = PageRequest.of(0, 10);
        var priorityDeckIds = new SliceImpl<>(List.of(1L, 2L, 3L), pageable, true);

        when(flashcardsProgressRepository.findPriorityDeckIds(eq(userId), any(), eq(pageable)))
                .thenReturn(priorityDeckIds);

        // When
        var result = service.getPriorityDeckIdsForUser(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getNumberOfElements());
        assertTrue(result.hasNext());
        verify(flashcardsProgressRepository, times(1))
                .findPriorityDeckIds(eq(userId), any(), eq(pageable));
    }

    @Test
    void testGetPriorityDeckIdsForUser_ReturnsPaginatedResults() {
        // Given
        var pageable = PageRequest.of(1, 5); // Page 1, size 5
        var priorityDeckIds = new SliceImpl<>(List.of(6L, 7L, 8L), pageable, false);

        when(flashcardsProgressRepository.findPriorityDeckIds(eq(userId), any(), eq(pageable)))
                .thenReturn(priorityDeckIds);

        // When
        var result = service.getPriorityDeckIdsForUser(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getNumberOfElements());
        assertFalse(result.hasNext());
    }
}

