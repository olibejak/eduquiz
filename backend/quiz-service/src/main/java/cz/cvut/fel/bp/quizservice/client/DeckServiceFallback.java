package cz.cvut.fel.bp.quizservice.client;

import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class DeckServiceFallback implements DeckServiceClient {

    @Override
    public QuestionDTO getQuestionById(Long id) {
        return null;
    }

    @Override
    public List<Long> getQuestionIdsForDeck(Long id) {
        return List.of();
    }

    @Override
    public Map<Long, String> getDeckNames(List<Long> ids) {
        return Collections.emptyMap();
    }
}
