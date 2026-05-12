package cz.cvut.fel.bp.flashcardsservice.properties;

import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "flashcards.spaced-repetition")
public class SpacedRepetitionProperties {

    private Map<FlashcardRating, SpacedRepetitionRule> rules;

    @Data
    public static class SpacedRepetitionRule {
        private double multiplier;
        private int minDays;
    }
}
