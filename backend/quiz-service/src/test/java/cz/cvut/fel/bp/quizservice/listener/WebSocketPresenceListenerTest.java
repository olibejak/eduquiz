package cz.cvut.fel.bp.quizservice.listener;

import cz.cvut.fel.bp.quizservice.service.facade.QuizParticipantFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketPresenceListenerTest {

    @Mock
    private QuizParticipantFacade participantFacade;

    private WebSocketPresenceListener listener;

    @BeforeEach
    void setUp() {
        listener = new WebSocketPresenceListener(participantFacade);
    }

    @Test
    void handleSessionConnectedMarksParticipantAsConnected() {
        Message<byte[]> message = messageWithPrincipal();
        SessionConnectedEvent event = mock(SessionConnectedEvent.class);
        when(event.getMessage()).thenReturn(message);

        listener.handleSessionConnected(event);

        verify(participantFacade).updateConnectionStatus(7L, true);
    }

    @Test
    void handleSessionDisconnectMarksParticipantAsDisconnected() {
        Message<byte[]> message = messageWithPrincipal();
        SessionDisconnectEvent event = mock(SessionDisconnectEvent.class);
        when(event.getMessage()).thenReturn(message);

        listener.handleSessionDisconnect(event);

        verify(participantFacade).updateConnectionStatus(7L, false);
    }

    private Message<byte[]> messageWithPrincipal() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setUser(() -> "7");
        accessor.setSessionAttributes(java.util.Map.of("participantId", 7L));
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}



