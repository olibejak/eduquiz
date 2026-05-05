package cz.cvut.fel.bp.flashcardsservice.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO representing a question that will be used for flashcard.
 * @param id - The ID of the question.
 * @param text - The text of the question.
 * @param questionType - The type of the question. One of: {"STANDARD", "CHOICE", "MATCHING"}
 * @param answers - The list of answers associated with the question.
 */
@Builder
public record QuestionDTO(
        Long id,
        String text,
        String questionType,
        List<AnswerDTO> answers,
        Integer duration
) {}
