package cz.cvut.fel.bp.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class QuizHistory extends AbstractEntity {

    @Column(nullable = false)
    private LocalDateTime playedAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserQuizResult> results = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "quiz_history_deck_titles", joinColumns = @JoinColumn(name = "quiz_history_id"))
    @Column(name = "deck_title")
    @Builder.Default
    private List<String> deckTitles = new ArrayList<>();

    public void addResult(UserQuizResult result) {
        results.add(result);
        result.setQuiz(this);
    }

    public void addDeckTitle(String deckTitle) {
        deckTitles.add(deckTitle);
    }
}