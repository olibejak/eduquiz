package cz.cvut.fel.bp.quizservice.dto.question;

import java.util.List;

public record QuestionDTO(
        Long id,
        String text,
        String questionType,
        List<AnswerDTO> answers,
        Integer duration
) {}

