package cz.cvut.fel.bp.deckservice.controller;

import cz.cvut.fel.bp.deckservice.service.facade.DeckServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/decks")
@RequiredArgsConstructor
public class InternalDeckController {

    private final DeckServiceFacade deckServiceFacade;

    @GetMapping("/{deckId}/name")
    public String getDeckName(@PathVariable Long deckId) {
        return deckServiceFacade.getDeckName(deckId);
    }

    @GetMapping("/names")
    public Map<Long, String> getDeckNames(@RequestParam("ids") List<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) return Map.of();
        return deckServiceFacade.getDeckNames(deckIds);
    }

    @GetMapping("/{deckId}/total-count")
    public Integer getTotalQuestionsCount(@PathVariable Long deckId) {
        return deckServiceFacade.getTotalQuestionsCount(deckId);
    }

    @GetMapping("/{deckId}/question-ids")
    public List<Long> getAllQuestionIdsForDeck(@PathVariable Long deckId) {
        return deckServiceFacade.getAllQuestionIdsForDeck(deckId);
    }
}
