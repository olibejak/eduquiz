package cz.cvut.fel.bp.quizservice.model;

import cz.cvut.fel.bp.quizservice.model.converter.LongListConverter;
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
    private Integer currentQuestionIndex = 0;
    @Convert(converter = LongListConverter.class)
    @Builder.Default
    private List<Long> questionIds = new ArrayList<>();

    public long getCurrentQuestionId() {
        return questionIds.get(currentQuestionIndex);
    }
}