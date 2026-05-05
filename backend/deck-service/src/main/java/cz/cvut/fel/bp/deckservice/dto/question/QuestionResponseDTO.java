package cz.cvut.fel.bp.deckservice.dto.question;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerResponseDTO;
import cz.cvut.fel.bp.deckservice.model.QuestionType;

import java.util.List;

/**
 * DTO type outgoing to client when fetching a question.
 * @param id - The ID of the question.
 * @param text - The text of the question.
 * @param questionType - The type of the question. One of: {"STANDARD", "MULTIPLE_CHOICE", "MATCHING"}
 * @param answers - The list of answers associated with the question.
 */
public record QuestionResponseDTO(
        Long id,
        String text,
        QuestionType questionType,
        List<AnswerResponseDTO> answers,
        Integer duration
) {}
