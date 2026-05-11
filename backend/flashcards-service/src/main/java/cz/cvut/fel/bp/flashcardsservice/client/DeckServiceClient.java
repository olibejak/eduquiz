package cz.cvut.fel.bp.flashcardsservice.client;

import cz.cvut.fel.bp.flashcardsservice.dto.QuestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "deck-service", url = "${microservices.deck-service.url}")
public interface DeckServiceClient {

    @GetMapping("/api/internal/decks/{deckId}/name")
    String getDeckName(@PathVariable Long deckId);

    @GetMapping("/api/internal/decks/names")
    Map<Long, String> getDeckNames(@RequestParam("ids") List<Long> deckIds);

    @GetMapping("/api/internal/decks/{deckId}/total-count")
    Integer getTotalQuestionsCount(@PathVariable Long deckId);

    @GetMapping("/api/internal/questions")
    List<QuestionDTO> getQuestionsDetailsByIds(@RequestParam("ids") List<Long> sessionIds);

    @GetMapping("/api/internal/decks/{deckId}/question-ids")
    List<Long> getAllQuestionIdsForDeck(@PathVariable Long deckId);
}
