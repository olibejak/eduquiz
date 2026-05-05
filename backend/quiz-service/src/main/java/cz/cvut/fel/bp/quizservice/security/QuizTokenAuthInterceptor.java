package cz.cvut.fel.bp.quizservice.security;

import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.repository.QuizParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizTokenAuthInterceptor implements ChannelInterceptor {

    private final QuizParticipantRepository participantRepository;

    /**
     * Intercepts incoming WebSocket messages to authenticate users based on a token provided.
     * Sets the participant role.
     * @param message the incoming WebSocket message
     * @param channel the message channel
     * @return the message to be processed further, or null to stop processing
     */
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assert accessor != null;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("token");

            if (token == null) {
                throw new IllegalArgumentException("Invalid token");
            }

            QuizParticipant participant = participantRepository.findByToken(token);
            if (participant != null) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(participant.getRole().toString());

                // TODO: create own token -- temporally using UsernamePasswordAuthenticationToken
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        participant.getId(),
                        null,
                        List.of(authority)
                );

                accessor.setUser(auth);

                Objects.requireNonNull(accessor.getSessionAttributes()).put("participantId", participant.getId());

                log.debug("User {} connected via WS with role {}", participant.getId(), participant.getRole());
            } else {
                throw new IllegalArgumentException("Invalid token");
            }
        }
        return message;
    }

    /*
    /**
     * Intercepts incoming WebSocket messages to authenticate users based on a Bearer token provided
     * in the "Authorization" header during the CONNECT phase.
     * Sets the user principal for the WebSocket session.
     * @param message the incoming WebSocket message
     * @param channel the message channel
     * @return the message to be processed further, or null to stop processing
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("action=websocketConnect reason=missingBearerToken");
                throw new IllegalArgumentException("Missing Bearer token");
            }
            String token = authHeader.substring(7);
            QuizParticipant participant = participantRepository.findByToken(token);
            if (participant == null) {
                log.warn("action=websocketConnect reason=participantNotFound");
                throw new IllegalArgumentException("Participant not found for token");
            }
            Principal userPrincipal = () -> String.valueOf(participant.getId());
            accessor.setUser(userPrincipal);
            log.debug("action=websocketConnect userId={} lobbyPin={} result=authenticated", participant.getId(), participant.getSessionPin());
        }

        return message;
    }
     */
}
