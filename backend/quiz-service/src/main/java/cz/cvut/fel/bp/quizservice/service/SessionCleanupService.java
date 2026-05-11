package cz.cvut.fel.bp.quizservice.service;

import cz.cvut.fel.bp.quizservice.model.QuizSession;
import cz.cvut.fel.bp.quizservice.repository.QuizSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupService {

    private final QuizSessionRepository sessionRepository;

    @Value("${quiz.cleanup.empty-lobby-timeout-minutes:30}")
    private long emptyLobbyTimeoutMinutes;

    /**
     * Periodically removes sessions that have been empty for longer than the configured timeout.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRateString = "${quiz.cleanup.interval-ms:300000}")
    @Transactional
    public void cleanupEmptyLobbies() {
        List<QuizSession> emptySessions = sessionRepository
                .findAllByEmptySinceAtIsNotNullAndEmptySinceAtBefore(
                        java.time.LocalDateTime.now().minusMinutes(emptyLobbyTimeoutMinutes)
                );

        if (emptySessions.isEmpty()) {
            return;
        }

        emptySessions.forEach(session -> {
            try {
                sessionRepository.delete(session);
                log.info("action=cleanupEmptyLobbies lobbyPin={} reason=emptyTimeout minutes={}",
                    session.getLobbyPin(), emptyLobbyTimeoutMinutes);
            } catch (Exception e) {
                log.error("action=cleanupEmptyLobbies lobbyPin={} error={}",
                    session.getLobbyPin(), e.getMessage());
            }
        });

        log.info("action=cleanupEmptyLobbies cleaned={} sessions timeout={}min",
            emptySessions.size(), emptyLobbyTimeoutMinutes);
    }
}

