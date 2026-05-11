package cz.cvut.fel.bp.quizservice.security;

import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.model.SessionState;
import cz.cvut.fel.bp.quizservice.repository.QuizParticipantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import java.util.Optional;

import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.session;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityComponentsTest {

    @Mock
    private QuizParticipantRepository participantRepository;

    @Mock
    private MessageChannel messageChannel;

    @Test
    void tokenAuthInterceptorSetsPrincipalForConnectMessages() {
        QuizParticipant participant = participant(7L, session("PIN123", UUID.randomUUID(), SessionState.LOBBY), UUID.randomUUID(), "Alice", null, "token-123", 0, true);
        QuizTokenAuthInterceptor interceptor = new QuizTokenAuthInterceptor(participantRepository);
        when(participantRepository.findByToken("token-123")).thenReturn(participant);

        Message<byte[]> message = connectMessageWithToken("token-123");
        Message<?> result = interceptor.preSend(message, messageChannel);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
        Principal principal = accessor.getUser();
        assertThat(principal).isNotNull();
        assertThat(principal.getName()).isEqualTo("7");
    }

    @Test
    void tokenAuthInterceptorRejectsMissingToken() {
        QuizTokenAuthInterceptor interceptor = new QuizTokenAuthInterceptor(participantRepository);
        Message<byte[]> message = connectMessageWithoutToken();

        assertThatThrownBy(() -> interceptor.preSend(message, messageChannel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void customJwtConverterBuildsAuthenticationToken() {
        CustomJwtConverter converter = new CustomJwtConverter();
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("userId", userId.toString())
                .claim("role", "USER")
                .claim("username", "alice")
                .subject("subject-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);
        UserPrincipal principal = (UserPrincipal) Optional.ofNullable(token)
                .map(AbstractAuthenticationToken::getPrincipal)
                .orElseThrow();

        assertThat(Optional.of(principal).map(UserPrincipal::id).orElseThrow()).isEqualTo(userId);
        assertThat(principal.username()).isEqualTo("alice");
        assertThat(principal.role()).isEqualTo("USER");
        assertThat(token.getAuthorities()).extracting("authority").containsExactly("USER");
    }

    @Test
    void customJwtConverterRejectsInvalidClaims() {
        CustomJwtConverter converter = new CustomJwtConverter();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("userId", "not-a-uuid")
                .claim("role", "USER")
                .claim("username", "alice")
                .subject("subject-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Message<byte[]> connectMessageWithToken(String token) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("token", token);
        accessor.setLeaveMutable(true);
        accessor.setSessionAttributes(new java.util.HashMap<>());
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Message<byte[]> connectMessageWithoutToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.setSessionAttributes(new java.util.HashMap<>());
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}









