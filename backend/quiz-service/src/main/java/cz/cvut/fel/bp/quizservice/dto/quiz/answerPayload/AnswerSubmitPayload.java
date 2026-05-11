package cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "answerType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MatchingAnswerSubmitPayload.class, name = "MATCHING"),
        @JsonSubTypes.Type(value = ChoiceAnswerSubmitPayload.class, name = "CHOICE"),
        @JsonSubTypes.Type(value = StandardAnswerSubmitPayload.class, name = "STANDARD")
})
public interface AnswerSubmitPayload {
}
