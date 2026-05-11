package cz.cvut.fel.bp.quizservice.listener;

import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.service.event.ParticipantConnectionChangedEvent.Status;
import cz.cvut.fel.bp.quizservice.service.facade.EventPublisherFacade;
import cz.cvut.fel.bp.quizservice.service.facade.QuizParticipantFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketPresenceListener {

    private final QuizParticipantFacade participantFacade;
    private final EventPublisherFacade eventPublisherFacade;

    @EventListener
    @Transactional
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            log.info("Attributes are null ");
            return;
        }

        Long participantId = (Long) attributes.get("participantId");

        if (participantId != null) {
            log.info("action=participantConnected participantId={}", participantId);
            try {
                participantFacade.updateConnectionStatus(participantId, true);
                QuizParticipant participant = participantFacade.findById(participantId);
                eventPublisherFacade.publishParticipantConnectionChanged(
                        participant.getSessionPin(), participant.getId(), participant.getNickname(), Status.CONNECTED
                );
            } catch (Exception e) {
                log.error("Failed to publish connection event", e);
            }
        }
    }

    @EventListener
    @Transactional
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = accessor.getSessionAttributes();

        if (attributes == null) return;

        Long participantId = (Long) attributes.get("participantId");

        if (participantId != null) {
            log.info("action=participantDisconnected participantId={}", participantId);

            try {
                boolean exists = participantFacade.existsById(participantId);

                if (exists) {
                    participantFacade.updateConnectionStatus(participantId, false);
                    QuizParticipant participant = participantFacade.findById(participantId);

                    eventPublisherFacade.publishParticipantConnectionChanged(
                            participant.getSessionPin(), participant.getId(), participant.getNickname(), Status.DISCONNECTED
                    );
                } else {
                    log.debug("Participant {} not found. Likely left explicitly via HTTP.", participantId);
                }
            } catch (Exception e) {
                log.debug("Could not process disconnect for participant {}: {}", participantId, e.getMessage());
            }
        }
    }
}