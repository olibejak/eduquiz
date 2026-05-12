package cz.cvut.fel.bp.deckservice.dto.question;

import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;


/**
 * DTO type incoming from client when creating/updating a question.
 * @param text - The text of the question.
 * @param questionType - The type of the question. One of: {"STANDARD", "MULTIPLE_CHOICE", "MATCHING"}
 * @param answers - The list of answers associated with the question.
 */
public record QuestionRequestDTO (
        @NotBlank(message = "Text must not be blank")
        @Size(max = 500, message = "Text must not exceed 500 characters")
        String text,
        @NotNull(message = "Question type must not be null")
        QuestionType questionType,
        @NotNull(message = "Answer list must not be null")
        @NotEmpty(message = "Answer list must not be empty")
        @Valid
        List<AnswerRequestDTO> answers,
        @NotNull
        @Positive
        Integer duration
) {}
