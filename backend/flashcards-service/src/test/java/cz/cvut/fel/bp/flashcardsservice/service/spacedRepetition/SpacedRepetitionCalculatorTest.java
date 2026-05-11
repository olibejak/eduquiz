package cz.cvut.fel.bp.flashcardsservice.service.spacedRepetition;

import cz.cvut.fel.bp.flashcardsservice.exception.InvalidFlashcardRatingException;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import cz.cvut.fel.bp.flashcardsservice.properties.SpacedRepetitionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacedRepetitionCalculatorTest {

    private SpacedRepetitionProperties properties;

    private SpacedRepetitionCalculator calculator;
    private Map<FlashcardRating, SpacedRepetitionProperties.SpacedRepetitionRule> rules;

    @BeforeEach
    void setUp() {
        properties = new SpacedRepetitionProperties();
        rules = new HashMap<>();

        // Setup rules as per configuration
        rules.put(FlashcardRating.AGAIN, createRule(0.0, 0));
        rules.put(FlashcardRating.HARD, createRule(1.2, 1));
        rules.put(FlashcardRating.GOOD, createRule(2.5, 1));
        rules.put(FlashcardRating.EXCELLENT, createRule(3.5, 4));

        properties.setRules(rules);
        calculator = new SpacedRepetitionCalculator(properties);
    }

    @Test
    void testCalculateNextInterval_AGAINReturnsZero() {
        // When
        var result = calculator.calculateNextInterval(FlashcardRating.AGAIN, 10);

        // Then
        assertEquals(0, result);
    }

    @Test
    void testCalculateNextInterval_GOODMultipliesInterval() {
        // Given
        int currentInterval = 2;
        // GOOD has multiplier 2.5, so 2 * 2.5 = 5 days

        // When
        var result = calculator.calculateNextInterval(FlashcardRating.GOOD, currentInterval);

        // Then
        assertEquals(5, result);
    }

    @Test
    void testCalculateNextInterval_HARDRespectMinDays() {
        // Given
        int currentInterval = 0;
        // HARD has multiplier 1.2 and minDays 1
        // 0 * 1.2 = 0, but minDays = 1, so result should be 1

        // When
        var result = calculator.calculateNextInterval(FlashcardRating.HARD, currentInterval);

        // Then
        assertEquals(1, result);
    }

    @Test
    void testCalculateNextInterval_EXCELLENTMaximizesInterval() {
        // Given
        int currentInterval = 3;
        // EXCELLENT has multiplier 3.5 and minDays 4
        // 3 * 3.5 = 10.5, rounded to 11, which > 4

        // When
        var result = calculator.calculateNextInterval(FlashcardRating.EXCELLENT, currentInterval);

        // Then
        assertEquals(11, result);
    }

    @Test
    void testCalculateNextInterval_ThrowsExceptionForMissingRule() {
        // Given
        FlashcardRating invalidRating = FlashcardRating.AGAIN;
        properties.setRules(new HashMap<>()); // Empty rules on real object

        // When & Then
        assertThrows(InvalidFlashcardRatingException.class,
                () -> calculator.calculateNextInterval(invalidRating, 5));
    }

    @Test
    void testCalculateNextInterval_RoundsDecimalValues() {
        // Given
        int currentInterval = 3;
        // GOOD: 3 * 2.5 = 7.5, which rounds to 8

        // When
        var result = calculator.calculateNextInterval(FlashcardRating.GOOD, currentInterval);

        // Then
        // 3 * 2.5 = 7.5, rounded to 8
        assertEquals(8, result);
    }

    @Test
    void testCalculateNextReview_AGAINSchedulesIn10Minutes() {
        // When
        var result = calculator.calculateNextReview(0);

        // Then
        assertNotNull(result);
        var now = LocalDateTime.now();
        var expected = now.plusMinutes(10);

        // Allow 2 second tolerance for test execution time
        assertTrue(result.isAfter(expected.minusSeconds(2)));
        assertTrue(result.isBefore(expected.plusSeconds(2)));
    }

    @Test
    void testCalculateNextReview_SchedulesCorrectDaysLater() {
        // Given
        int intervalDays = 5;

        // When
        var result = calculator.calculateNextReview(intervalDays);

        // Then
        assertNotNull(result);
        var now = LocalDateTime.now();
        var expected = now.plusDays(5);

        // Allow 2 second tolerance
        assertTrue(result.isAfter(expected.minusSeconds(2)));
        assertTrue(result.isBefore(expected.plusSeconds(2)));
    }

    @Test
    void testCalculateNextReview_Handles1Day() {
        // When
        var result = calculator.calculateNextReview(1);

        // Then
        assertNotNull(result);
        var now = LocalDateTime.now();
        var expected = now.plusDays(1);

        // Allow 2 second tolerance
        assertTrue(result.isAfter(expected.minusSeconds(2)));
        assertTrue(result.isBefore(expected.plusSeconds(2)));
    }

    @Test
    void testCalculateNextReview_HandlesLongIntervals() {
        // When
        var result = calculator.calculateNextReview(30);

        // Then
        assertNotNull(result);
        var now = LocalDateTime.now();
        var expected = now.plusDays(30);

        // Allow 2 second tolerance
        assertTrue(result.isAfter(expected.minusSeconds(2)));
        assertTrue(result.isBefore(expected.plusSeconds(2)));
    }

    private SpacedRepetitionProperties.SpacedRepetitionRule createRule(double multiplier, int minDays) {
        var rule = new SpacedRepetitionProperties.SpacedRepetitionRule();
        rule.setMultiplier(multiplier);
        rule.setMinDays(minDays);
        return rule;
    }
}
