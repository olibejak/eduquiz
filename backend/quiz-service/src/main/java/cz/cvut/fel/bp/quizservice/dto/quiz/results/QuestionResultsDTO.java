package cz.cvut.fel.bp.quizservice.dto.quiz.results;

import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;

import java.util.List;

public record QuestionResultsDTO(
        QuestionDTO question,
        List<ParticipantQuestionResultsDTO> participantResults
) {}
