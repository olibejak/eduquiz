package cz.cvut.fel.bp.quizservice.service.event;

import cz.cvut.fel.bp.quizservice.dto.quiz.results.QuestionResultsDTO;

public record QuestionEndedEvent(
        String lobbyPin,
        QuestionResultsDTO results
) {}
