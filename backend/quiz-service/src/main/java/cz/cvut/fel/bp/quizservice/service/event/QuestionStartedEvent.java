package cz.cvut.fel.bp.quizservice.service.event;

import cz.cvut.fel.bp.quizservice.dto.quiz.QuizQuestionDTO;

public record QuestionStartedEvent(
        String lobbyPin,
        QuizQuestionDTO question
) {}
