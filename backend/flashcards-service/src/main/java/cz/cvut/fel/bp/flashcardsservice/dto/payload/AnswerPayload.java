package cz.cvut.fel.bp.flashcardsservice.dto.payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for all answer payloads.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChoiceAnswerPayload.class, name = "CHOICE"),
        @JsonSubTypes.Type(value = MatchingAnswerPayload.class, name = "MATCHING"),
})
public sealed interface AnswerPayload
        permits ChoiceAnswerPayload, MatchingAnswerPayload {
}
