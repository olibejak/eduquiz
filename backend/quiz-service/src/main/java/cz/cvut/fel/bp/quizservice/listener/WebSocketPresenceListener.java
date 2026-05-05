package cz.cvut.fel.bp.quizservice.listener;

import cz.cvut.fel.bp.quizservice.service.facade.QuizParticipantFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketPresenceListener {

    private final QuizParticipantFacade participantFacade;

    /**
     * Listens to connection events and updates the participant's connection status in the database.
     * Expects the participantId to be stored in the WebSocket session attributes when the connection is established.
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long participantId = (Long) Objects.requireNonNull(accessor.getSessionAttributes()).get("participantId");

        if (participantId != null) {
            log.info("action=participantConnected participantId={}", participantId);
            participantFacade.updateConnectionStatus(participantId, true); //
        } else {
            log.warn("action=participantConnected reason=missingParticipantIdInSession");
        }
    }

    /**
     * Listens to disconnection events and updates the participant's connection status in the database.
     * @param event the session disconnect event containing the user principal with the participantId
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long participantId = (Long) Objects.requireNonNull(accessor.getSessionAttributes()).get("participantId");

        if (participantId != null) {
            log.info("action=participantDisconnected participantId={}", participantId);
            participantFacade.updateConnectionStatus(participantId, false);
        } else {
            log.warn("action=participantDisconnected reason=missingPrincipal");
        }
    }
}
