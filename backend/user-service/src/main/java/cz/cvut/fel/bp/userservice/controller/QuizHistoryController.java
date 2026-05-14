package cz.cvut.fel.bp.userservice.controller;

import cz.cvut.fel.bp.userservice.dto.quiz.QuizLeaderboardDTO;
import cz.cvut.fel.bp.userservice.dto.quiz.UserHistoryItemDTO;
import cz.cvut.fel.bp.userservice.security.UserPrincipal;
import cz.cvut.fel.bp.userservice.service.QuizHistoryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/history")
@CircuitBreaker(name = "userQuizHistoryApi")
@RequiredArgsConstructor
public class QuizHistoryController {

    private final QuizHistoryService quizHistoryService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<List<UserHistoryItemDTO>> getMyHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<UserHistoryItemDTO> history = quizHistoryService.getUserHistory(userPrincipal.id());
        return ResponseEntity.ok(history);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<QuizLeaderboardDTO> getSessionLeaderboard(
            @PathVariable Long sessionId) {

        QuizLeaderboardDTO leaderboard = quizHistoryService.getQuizLeaderboard(sessionId);
        return ResponseEntity.ok(leaderboard);
    }
}