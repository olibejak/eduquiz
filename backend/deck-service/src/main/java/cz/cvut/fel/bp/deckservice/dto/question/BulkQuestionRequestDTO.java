package cz.cvut.fel.bp.deckservice.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record BulkQuestionRequestDTO(
        @NotNull Map<Long, @Valid QuestionRequestDTO> updates,

        @NotNull List<@Valid QuestionRequestDTO> creates
) {}