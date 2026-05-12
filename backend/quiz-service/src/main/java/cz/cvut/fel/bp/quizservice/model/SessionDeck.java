package cz.cvut.fel.bp.quizservice.model;

import cz.cvut.fel.bp.quizservice.model.converter.LongArrayConverter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Convert;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Embeddable
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionDeck {
    private Long deckId;
    private Integer playOrder;
    @Builder.Default
    private Integer currentQuestionIndex = -1;
    @Convert(converter = LongArrayConverter.class)
    @Builder.Default
    private Long[] questionIds = new Long[0];

    public Long getCurrentQuestionId() {
        if (questionIds == null || currentQuestionIndex >= questionIds.length) {
            return null;
        }
        return questionIds[currentQuestionIndex];
    }}