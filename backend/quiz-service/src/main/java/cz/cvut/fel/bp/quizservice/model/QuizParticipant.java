package cz.cvut.fel.bp.quizservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_participant")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class QuizParticipant extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private QuizSession session;

    // Info: null in case of anonym
    @Column(nullable = true)
    private UUID userId;

    @Column(nullable = false)
    private String nickname;

    @Builder.Default
    private Integer currentScore = 0;

    @Builder.Default
    private Boolean isCurrentCorrect = null;

    @Column(nullable = false)
    @Builder.Default
    private ParticipantRole role = ParticipantRole.USER;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastActiveAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean isConnected = true;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSpectating = false;

    /**
     * Updates the lastActiveAt timestamp to the current time.
     * This method should be called whenever the participant performs an actio.
     */
    @PostUpdate
    protected void updateLastActive() {
        super.onUpdate();
        this.lastActiveAt = LocalDateTime.now();
    }

    public void addPoints(Integer points) {
        this.currentScore += points;
    }

    public String getSessionPin() {
        return session != null ? session.getLobbyPin() : null;
    }
}