package cz.cvut.fel.bp.flashcardsservice.controller;

import cz.cvut.fel.bp.flashcardsservice.dto.QuestionDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.deck.DeckProgressStatusDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.deck.DeckProgressSummaryDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.FlashcardReviewBatchDTO;
import cz.cvut.fel.bp.flashcardsservice.security.UserPrincipal;
import cz.cvut.fel.bp.flashcardsservice.service.facade.FlashcardProgressFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressController {

    private final FlashcardProgressFacade flashcardProgressFacade;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public Slice<DeckProgressSummaryDTO> getDueDeckFoUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal, Pageable pageable) {
        log.debug("Get due decks dashboard request userId={}, page={}, size={}", userPrincipal.id(), pageable.getPageNumber(), pageable.getPageSize());

        Slice<DeckProgressSummaryDTO> result = flashcardProgressFacade.getDueDecksDashboard(userPrincipal.id(), pageable);
        
        log.debug("Due decks dashboard retrieved userId={}, deckCount={}", userPrincipal.id(), result.getNumberOfElements());
        return result;
    }

    @GetMapping("/{deckId}")
    @PreAuthorize("hasRole('USER')")
    public DeckProgressStatusDTO getDeckProgressStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long deckId) {
        log.debug("Get deck progress status request userId={}, deckId={}", userPrincipal.id(), deckId);

        DeckProgressStatusDTO result = flashcardProgressFacade.getDeckStatus(userPrincipal.id(), deckId);
        
        log.debug("Deck progress status retrieved userId={}, deckId={}, due={}, newCount={}", 
                userPrincipal.id(), deckId, result.dueCount(), result.newCount());
        return result;
    }

    @PostMapping("/{deckId}")
    @PreAuthorize("hasRole('USER')")
    public void submitFlashcardReviewBatch(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long deckId,
            @RequestBody FlashcardReviewBatchDTO batch) {
        log.info("Submit flashcard review batch userId={}, deckId={}, reviewCount={}",
                userPrincipal.id(), deckId, batch.reviews().size());

        flashcardProgressFacade.submitCardReviewBatch(userPrincipal.id(), deckId, batch);

        log.debug("Flashcard review batch processed userId={}, deckId={}, reviewCount={}", 
                userPrincipal.id(), deckId, batch.reviews().size());
    }

    @GetMapping("/{deckId}/session")
    public ResponseEntity<List<QuestionDTO>> getStudySession(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("Get study session request userId={}, deckId={}, size={}", principal.id(), deckId, size);

        List<QuestionDTO> sessionCards = flashcardProgressFacade.getStudySession(principal.id(), deckId, size);

        log.debug("Study session generated userId={}, deckId={}, cardCount={}", principal.id(), deckId, sessionCards.size());
        return ResponseEntity.ok(sessionCards);
    }
}
