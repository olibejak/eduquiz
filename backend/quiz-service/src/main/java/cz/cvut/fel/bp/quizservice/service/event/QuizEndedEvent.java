package cz.cvut.fel.bp.quizservice.service.event;

import cz.cvut.fel.bp.quizservice.dto.UserQuizResultsDTO;

import java.time.LocalDateTime;
import java.util.List;

public record QuizEndedEvent(
        String lobbyPin,
        List<UserQuizResultsDTO> results,
        LocalDateTime finishedAt,
        List<String> deckTitles
) {}
