package cz.cvut.fel.bp.quizservice.client;

import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "deck-service", url = "${microservices.deck-service.url}")
public interface DeckServiceClient {

    @Cacheable(value = "questions", key = "#id")
    @GetMapping("/api/internal/questions/{id}")
    QuestionDTO getQuestionById(@NotNull @PathVariable Long id);

    @Cacheable(value = "questionIds", key = "#id")
    @GetMapping("/api/internal/decks/{id}/questions/ids")
    List<Long> getQuestionIdsForDeck(@NotNull @PathVariable Long id);
}
