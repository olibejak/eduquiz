package cz.cvut.fel.bp.quizservice.dto.question;

import cz.cvut.fel.bp.quizservice.dto.question.answerPayload.AnswerPayload;
import lombok.Builder;

@Builder
public record AnswerDTO(
        Long id,
        String text,
        String type,
        AnswerPayload payload
) {}

