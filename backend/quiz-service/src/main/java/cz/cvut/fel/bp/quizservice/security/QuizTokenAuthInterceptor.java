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
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizTokenAuthInterceptor implements ChannelInterceptor {

    private final QuizParticipantRepository participantRepository;

    // Todo: refactor
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            log.debug("STOMP Interceptor: captured command = {}", accessor.getCommand());

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                log.info("STOMP Interceptor: processing CONNECT frame. Headers: {}", accessor.toMap());

                //String token = accessor.getFirstNativeHeader("token");

                String token = accessor.getFirstNativeHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                if (token != null && !token.trim().isEmpty()) {
                    log.info("STOMP Interceptor: token found = {}", token);

                    QuizParticipant participant = participantRepository.findByToken(token);

                    if (participant != null) {
                        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

                        if (sessionAttributes != null) {
                            sessionAttributes.put("participantId", participant.getId());
                            log.info("STOMP Interceptor: successfully stored participantId={} in sessionAttributes", participant.getId());

                            ParticipantPrincipal principal = new ParticipantPrincipal(String.valueOf(participant.getId()));
                            accessor.setUser(principal);
                            log.info("STOMP Interceptor: assigned Principal with name={}", principal.getName());
                        } else {
                            log.error("STOMP Interceptor: CRITICAL ERROR - sessionAttributes are NULL! This should not happen in WebSocket handling.");
                        }
                    } else {
                        log.warn("STOMP Interceptor: participant for token={} was not found in the database!", token);
                    }
                } else {
                    log.error("STOMP Interceptor: missing 'token' header! Unable to identify the user.");
                }
            }
        }
        return message;
    }

    /*
    private final QuizParticipantRepository participantRepository;

    /**
     * Intercepts incoming WebSocket messages to authenticate users based on a token provided.
     * Sets the participant role.
     * @param message the incoming WebSocket message
     * @param channel the message channel
     * @return the message to be processed further, or null to stop processing
     */
    /*
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            List<String> tokenHeaders = accessor.getNativeHeader("token");

            if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                String token = tokenHeaders.getFirst();

                QuizParticipant participant = participantRepository.findByToken(token);

                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes == null) {
                    sessionAttributes = new java.util.concurrent.ConcurrentHashMap<>();
                    accessor.setSessionAttributes(sessionAttributes);
                }

                if (participant != null) {
                    sessionAttributes.put("participantId", participant.getId());
                    log.info("Saved participantId={} for token={}", participant.getId(), token);
                } else {
                    log.warn("Participant token={} not found!", token);
                }
            }
        }
        return message;
    }
    */
}
