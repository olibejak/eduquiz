package cz.cvut.fel.bp.quizservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_session")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession extends AbstractEntity {

     @Column(nullable = false, unique = true, length = 10)
     private String lobbyPin;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private SessionState state;

     @Builder.Default
     private Integer currentDeckIndex = 0;

     @Builder.Default
     private Integer currentQuestionAnswersCount = 0;

     @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
     @Builder.Default
     private List<QuizParticipant> participants = new ArrayList<>();

     @ElementCollection
     @CollectionTable(name = "quiz_session_decks", joinColumns = @JoinColumn(name = "session_id"))
     @Builder.Default
     private List<SessionDeck> sessionDecks = new ArrayList<>();

     /**
      * Adds a participant to the session and sets the session reference in the participant.
      * @param participant the participant to add
      */
     public void addParticipant(QuizParticipant participant) {
          participants.add(participant);
          participant.setSession(this);
     }

     public void removeParticipant(QuizParticipant participant) {
          participants.remove(participant);
          participant.setSession(null);
     }

     public void addDeck(Long deckId, List<Long> questionIds) {
          if (sessionDecks.stream().anyMatch(d -> d.getDeckId().equals(deckId))) {
               throw new IllegalArgumentException("Deck with id " + deckId + " is already added to the session");
          }
          int newIndex = sessionDecks.size();
          sessionDecks.add(SessionDeck.builder()
                  .deckId(deckId)
                  .playOrder(newIndex)
                  .questionIds(questionIds)
                  .build()
          );
     }

     public void removeDeck(Long deckId) {
          boolean removed = sessionDecks.removeIf(deck -> deck.getDeckId().equals(deckId));
          if (!removed) {
               throw new IllegalArgumentException("Deck with id " + deckId + " is not part of the session");
          }

          for (int i = 0; i < sessionDecks.size(); i++) {
               sessionDecks.get(i).setPlayOrder(i);
          }
     }

     public SessionDeck getSessionDeck(int index) {
          return sessionDecks.get(index);
     }

     public int getCurrentQuestionIndex() {
          return getSessionDeck(currentDeckIndex).getCurrentQuestionIndex();
     }

     public long getCurrentQuestionId() {
          return getSessionDeck(currentDeckIndex).getCurrentQuestionId();
     }
}