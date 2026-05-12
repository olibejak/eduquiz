package cz.cvut.fel.bp.flashcardsservice.client;

import cz.cvut.fel.bp.flashcardsservice.dto.QuestionDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DeckServiceFallback implements DeckServiceClient {

    @Override
    public String getDeckName(Long deckId) {
        return "";
    }

    @Override
    public Map<Long, String> getDeckNames(List<Long> deckIds) {
        return Map.of();
    }

    @Override
    public Integer getTotalQuestionsCount(Long deckId) {
        return 0;
    }

    @Override
    public List<QuestionDTO> getQuestionsDetailsByIds(List<Long> sessionIds) {
        return List.of();
    }

    @Override
    public List<Long> getAllQuestionIdsForDeck(Long deckId) {
        return List.of();
    }
}
