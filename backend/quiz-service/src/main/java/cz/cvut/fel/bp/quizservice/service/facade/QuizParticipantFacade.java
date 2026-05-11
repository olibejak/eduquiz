package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.model.ParticipantRole;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.model.QuizSession;
import cz.cvut.fel.bp.quizservice.service.QuizParticipantService;
import cz.cvut.fel.bp.quizservice.service.QuizSessionService;
import cz.cvut.fel.bp.quizservice.service.event.ParticipantConnectionChangedEvent;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
public class QuizParticipantFacade {

    private final QuizParticipantService quizParticipantService;
    private final QuizSessionService quizSessionService;
    private final EventPublisherFacade eventPublisherFacade;
    private final EventPublisherFacade eventPublisher;

    public QuizParticipant findById(@NotNull Long participantId, @NotNull String pin) {
        QuizParticipant participant = quizParticipantService.findById(participantId);
        if (!pin.equals(participant.getSessionPin())) {
            throw new AccessDeniedException("Pin does not match.");
        }
        return participant;
    }

    public QuizParticipant findById(@NotNull Long participantId) {
        return quizParticipantService.findById(participantId);
    }

    @Transactional
    public void updateConnectionStatus(Long participantId, boolean isConnected) {
        QuizParticipant participant = quizParticipantService.changeConnectionStatus(participantId, isConnected);
        String pin = participant.getSession().getLobbyPin();

        if (participant.getRole() == ParticipantRole.HOST) {
            transferHost(participant, pin);
        }

        // Info: Clear empty flag if participant reconnects; mark as empty if all disconnect
        if (isConnected) {
            quizSessionService.clearEmptyFlag(pin);
        } else {
            quizSessionService.removeSessionIfEmpty(pin);
        }

        publishConnectionEvent(
                pin,
                participantId,
                participant.getNickname(),
                isConnected
                        ? ParticipantConnectionChangedEvent.Status.CONNECTED
                        : ParticipantConnectionChangedEvent.Status.DISCONNECTED
        );

        log.info("action=updateConnectionStatus lobbyPin={} participantId={} isConnected={}", pin, participantId, isConnected);
    }

    public void transferHost(QuizParticipant oldHost, String lobbyPin) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(lobbyPin);

        QuizParticipant newHost = session.getParticipants().stream()
                .filter(participant -> !Objects.equals(participant.getId(), oldHost.getId()))
                .findFirst()
                .orElse(null);

        if (newHost == null) {
            log.warn("action=transferHost lobbyPin={} oldHostId={} reason=noOtherParticipants", lobbyPin, oldHost.getId());
            return;
        }

        newHost.setRole(ParticipantRole.HOST);
        oldHost.setRole(ParticipantRole.USER);

        quizSessionService.save(session);

        eventPublisher.publishHostTransferEvent(
                lobbyPin,
                oldHost.getId(),
                newHost.getId()
        );
    }

    private void publishConnectionEvent(String lobbyPin,
                                        Long participantId,
                                        String nickname,
                                        ParticipantConnectionChangedEvent.Status status) {
        eventPublisherFacade.publishParticipantConnectionChanged(lobbyPin, participantId, nickname, status);
    }

    public QuizParticipant evaluate(Long participantId, int points, boolean isCorrect) {
        QuizParticipant participant = quizParticipantService.evaluate(participantId, points, isCorrect);
        log.info("action=addPoints lobbyPin={} participantId={} pointsEarned={} totalScore={} isCorrect={}",
                participant.getSessionPin(), participantId, points, participant.getCurrentScore(), isCorrect);
        return participant;
    }

    public QuizParticipant findByToken(String token) {
        return quizParticipantService.findByToken(token);
    }

    public void delete(QuizParticipant participant) {
        quizParticipantService.delete(participant);
        log.info("action=deleteParticipant participantId={} lobbyPin={}", participant.getId(), participant.getSessionPin());
    }

    public boolean existsById(Long participantId) {
        return quizParticipantService.existsById(participantId);
    }
}
