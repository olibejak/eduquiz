package cz.cvut.fel.bp.quizservice.controller;

import cz.cvut.fel.bp.quizservice.dto.join.CreateLobbyResponseDTO;
import cz.cvut.fel.bp.quizservice.dto.LobbySnapshotDTO;
import cz.cvut.fel.bp.quizservice.dto.join.JoinRequestDTO;
import cz.cvut.fel.bp.quizservice.dto.join.JoinResponseDTO;
import cz.cvut.fel.bp.quizservice.service.facade.QuizLobbyFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizSessionController {

    private final QuizLobbyFacade lobbyFacade;

    @PostMapping("/create")
    public CreateLobbyResponseDTO createLobby(@Valid @RequestBody JoinRequestDTO request) {
        log.debug("action=createLobby userId={} nickname={}", request.userId(), request.nickname());
        return lobbyFacade.createQuizSession(request);
    }

    @PostMapping("/{pin}/join")
    public JoinResponseDTO joinLobby(@PathVariable String pin, @Valid @RequestBody JoinRequestDTO request) {
        log.debug("action=joinLobby lobbyPin={} userId={} deviceId={} nickname={}", pin, request.userId(), request.deviceId(), request.nickname());
        return lobbyFacade.joinSession(pin, request);
    }

    @PreAuthorize("hasRole('HOST')")
    @DeleteMapping("/{pin}/participants/{participantId}")
    public ResponseEntity<Void> kickPlayer(
            @PathVariable String pin,
            @PathVariable Long participantId) {

        log.debug("action=kickPlayer lobbyPin={} participantId={}", pin, participantId);
        lobbyFacade.removeFromSession(pin, participantId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/{pin}/start")
    public ResponseEntity<Void> startSession(
            @PathVariable String pin) {

        log.debug("action=startSessionRequested lobbyPin={}", pin);
        lobbyFacade.startSession(pin);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/{pin}/decks/{deckId}")
    public ResponseEntity<Void> addDeck(
            @PathVariable String pin,
            @PathVariable Long deckId) {

        log.debug("action=addDeck lobbyPin={} deckId={}", pin, deckId);
        lobbyFacade.addDeckToSession(pin, deckId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('HOST')")
    @DeleteMapping("/{pin}/decks/{deckId}")
    public ResponseEntity<Void> removeDeck(
            @PathVariable String pin,
            @PathVariable Long deckId) {

        log.debug("action=removeDeck lobbyPin={} deckId={}", pin, deckId);
        lobbyFacade.removeDeckFromSession(pin, deckId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{pin}")
    public LobbySnapshotDTO getLobbySnapshot(@PathVariable String pin) {
        log.debug("action=getLobbySnapshot lobbyPin={}", pin);
        return lobbyFacade.getLobbySnapshot(pin);
    }
}
