package cz.cvut.fel.bp.flashcardsservice.service.spacedRepetition;

import cz.cvut.fel.bp.flashcardsservice.exception.InvalidFlashcardRatingException;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import cz.cvut.fel.bp.flashcardsservice.properties.SpacedRepetitionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Component responsible for calculating the next review interval for flashcards
 * based on the user's rating and the current interval.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpacedRepetitionCalculator {

    private final SpacedRepetitionProperties properties;

    /**
     * Calculates the next interval in days for a flashcard based on the user's rating and the current interval.
     * @param rating The user's rating of their recall of the flashcard (e.g., AGAIN, HARD, GOOD, EXCELLENT).
     * @param currentIntervalDays The current interval in days since the last review of the flashcard.
     * @return The next interval in days until the flashcard should be reviewed again.
     * @throws InvalidFlashcardRatingException if the rating is not configured
     */
    public int calculateNextInterval(FlashcardRating rating, int currentIntervalDays) {
        log.debug("Calculating next interval rating={}, currentIntervalDays={}", rating, currentIntervalDays);
        
        SpacedRepetitionProperties.SpacedRepetitionRule rule = properties.getRules().get(rating);

        if (rule == null) {
            log.error("Missing spaced repetition rule for rating={}", rating);
            throw new InvalidFlashcardRatingException(
                    "Missing configuration for spaced repetition rule: " + rating
            );
        }

        if (rating == FlashcardRating.AGAIN) {
            log.debug("AGAIN rating detected, returning 0 interval rating={}", rating);
            return 0;
        }

        int scaledIntervalDays = (int) Math.round(currentIntervalDays * rule.getMultiplier());
        int nextIntervalDays = Math.max(rule.getMinDays(), scaledIntervalDays);
        
        log.debug("Interval calculated rating={}, currentDays={}, scaledDays={}, finalDays={}, multiplier={}, minDays={}", 
                rating, currentIntervalDays, scaledIntervalDays, nextIntervalDays, rule.getMultiplier(), rule.getMinDays());
        
        return nextIntervalDays;
    }

    /**
     * Calculates the next review date based on the interval days.
     * @param intervalDays The number of days until the next review
     * @return The LocalDateTime when the card should be reviewed next
     */
    public LocalDateTime calculateNextReview(int intervalDays) {
        log.debug("Calculating next review date intervalDays={}", intervalDays);
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime nextReview;
        if (intervalDays == 0) {
            nextReview = now.plusMinutes(10);
            log.debug("Next review in 10 minutes intervalDays={}", intervalDays);
        } else {
            nextReview = now.plusDays(intervalDays);
            log.debug("Next review in {} days intervalDays={}", intervalDays, intervalDays);
        }

        return nextReview;
    }
}
