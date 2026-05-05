package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.client.DeckServiceClient;
import cz.cvut.fel.bp.quizservice.dto.join.CreateLobbyResponseDTO;
import cz.cvut.fel.bp.quizservice.dto.LobbySnapshotDTO;
import cz.cvut.fel.bp.quizservice.dto.join.JoinRequestDTO;
import cz.cvut.fel.bp.quizservice.dto.join.JoinResponseDTO;
import cz.cvut.fel.bp.quizservice.exception.DuplicateParticipantException;
import cz.cvut.fel.bp.quizservice.exception.InvalidSessionStateException;
import cz.cvut.fel.bp.quizservice.model.ParticipantRole;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.model.QuizSession;
import cz.cvut.fel.bp.quizservice.model.SessionState;
import cz.cvut.fel.bp.quizservice.service.QuizSessionService;
import cz.cvut.fel.bp.quizservice.service.event.ParticipantConnectionChangedEvent;
import cz.cvut.fel.bp.quizservice.service.event.SessionDeckChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizLobbyFacade {

    private final QuizSessionService quizSessionService;
    private final QuizParticipantFacade quizParticipantFacade;
    private final QuizGameFacade quizGameFacade;
    private final EventPublisherFacade eventPublisherFacade;
    private final DeckServiceClient deckServiceClient;

    public void startSession(String sessionPin) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(sessionPin);

        if (session.getState() != SessionState.LOBBY) {
            throw new InvalidSessionStateException("Quiz can be started only from lobby.");
        }

        if (session.getSessionDecks().isEmpty()) {
            throw new IllegalStateException("Cannot start quiz without any decks.");
        }

        quizSessionService.startSession(sessionPin);
        eventPublisherFacade.publishQuizStarted(sessionPin, quizSessionService.getTotalQuestionCount(sessionPin));
        quizGameFacade.moveToNextQuestion(sessionPin);
    }

    public CreateLobbyResponseDTO createQuizSession(JoinRequestDTO hostRequestDTO) {
        QuizParticipant host = ParticipantDirector.createParticipant(hostRequestDTO);
        host.setRole(ParticipantRole.HOST);
        QuizSession session = quizSessionService.createQuizSession(host);

        log.info("action=createQuizSession lobbyPin={} hostUserId={} hostNickname={}", session.getLobbyPin(), hostRequestDTO.userId(), hostRequestDTO.nickname());

        eventPublisherFacade.publishLobbyCreated(session.getLobbyPin(), host.getId());

        return new CreateLobbyResponseDTO(
                session.getLobbyPin(),
                host.getId(),
                host.getToken()
        );
    }

    /**
     * Adds participant to a quiz lobby.
     * @param sessionPin - lobby pin of the session to join
     * @param joinRequestDTO - DTO containing participant's nickname and userId
     */
    public JoinResponseDTO joinSession(String sessionPin, JoinRequestDTO joinRequestDTO) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(sessionPin);
        if (session.getState() != SessionState.LOBBY) {
            throw new InvalidSessionStateException("Session is not in lobby.");
        }

        boolean duplicateParticipant = session.getParticipants().stream().anyMatch(participant -> {
            if (joinRequestDTO.userId() != null) {
                return joinRequestDTO.userId().equals(participant.getUserId());
            }
            return joinRequestDTO.deviceId() != null && joinRequestDTO.deviceId().equals(participant.getDeviceId());
        });

        if (duplicateParticipant) {
            throw new DuplicateParticipantException("Participant already joined this session.");
        }

        QuizParticipant participant = ParticipantDirector.createParticipant(joinRequestDTO);
        session.addParticipant(participant);
        quizSessionService.save(session);
        log.info("action=joinSession lobbyPin={} userId={} deviceId={} nickname={} result=participantAdded", sessionPin, joinRequestDTO.userId(), joinRequestDTO.deviceId(), joinRequestDTO.nickname());
        eventPublisherFacade.publishParticipantConnectionChanged(sessionPin,
                participant.getId(), participant.getNickname(), ParticipantConnectionChangedEvent.Status.JOINED);
        return createJoinResponse(participant);
    }

    public void removeFromSession(String sessionPin, Long participantId) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(sessionPin);

        if (session.getState() != SessionState.LOBBY) {
            throw new InvalidSessionStateException("Session is not in lobby.");
        }

        QuizParticipant host = session.getParticipants().getFirst();

        QuizParticipant participant = quizParticipantFacade.findById(participantId, sessionPin);

        if (host.getId().equals(participant.getId())) {
            throw new IllegalArgumentException("Host cannot kick themself.");
        }

        session.removeParticipant(participant);
        quizSessionService.save(session);
        log.info("action=removeFromSession lobbyPin={} participantId={} result=participantRemoved", sessionPin, participantId);
        eventPublisherFacade.publishParticipantConnectionChanged(sessionPin,
                participant.getId(), participant.getNickname(), ParticipantConnectionChangedEvent.Status.KICKED);
    }

    public LobbySnapshotDTO getLobbySnapshot(String sessionPin) {
        QuizSession session = quizSessionService.findSessionByLobbyPin(sessionPin);
        if (session.getState() != SessionState.LOBBY) {
            throw new InvalidSessionStateException("Session is not in lobby.");
        }

        log.debug("action=getLobbySnapshot lobbyPin={} participantCount={}", sessionPin, session.getParticipants().size());

        List<LobbySnapshotDTO.ParticipantDTO> participantDTOs = session.getParticipants().stream()
                .map(participant -> new LobbySnapshotDTO.ParticipantDTO(
                        participant.getId(),
                        participant.getNickname(),
                        participant.getCurrentScore(),
                        participant.getIsConnected()
                ))
                .toList();

        return new LobbySnapshotDTO(
                session.getLobbyPin(),
                session.getState(),
                participantDTOs
        );
    }

    private JoinResponseDTO createJoinResponse(QuizParticipant participant) {
        return JoinResponseDTO.builder()
                .participantId(participant.getId())
                .nickname(participant.getNickname())
                .token(participant.getToken())
                .build();
    }

    public void addDeckToSession(String pin, Long deckId) {
        List<Long> deckIds = deckServiceClient.getQuestionIdsForDeck(deckId);
        quizSessionService.addDeckToQuizSession(pin, deckId, deckIds);
        eventPublisherFacade.publishSessionDeckChanged(pin, deckId, SessionDeckChangedEvent.ChangeType.ADDED);
    }

    public void removeDeckFromSession(String pin, Long deckId) {
        quizSessionService.removeDeckFromQuizSession(pin, deckId);
        eventPublisherFacade.publishSessionDeckChanged(pin, deckId, SessionDeckChangedEvent.ChangeType.REMOVED);
    }

    private static class ParticipantDirector {
        public static QuizParticipant createParticipant(JoinRequestDTO joinRequestDTO) {
            if (joinRequestDTO.userId() == null) {
                return createAnonymousParticipant(joinRequestDTO.deviceId(), joinRequestDTO.nickname());
            }
            return createUserParticipant(joinRequestDTO.userId(), joinRequestDTO.nickname());
        }

        private static QuizParticipant createAnonymousParticipant(String deviceId, String nickname) {
            if (deviceId == null || deviceId.isBlank()) {
                throw new IllegalArgumentException("Device ID is required for anonymous participants");
            }
            return buildParticipant(null, nickname, deviceId);
        }

        private static QuizParticipant createUserParticipant(UUID userId, String nickname) {
            return buildParticipant(userId, nickname, null);
        }

        private static QuizParticipant buildParticipant(UUID userId, String nickname, String deviceId) {
            return QuizParticipant.builder()
                    .userId(userId)
                    .nickname(nickname)
                    .deviceId(deviceId)
                    .token(UUID.randomUUID().toString())
                    .build();
        }
    }
}
