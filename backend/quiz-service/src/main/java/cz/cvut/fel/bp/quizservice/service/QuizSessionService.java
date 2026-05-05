package cz.cvut.fel.bp.quizservice.service;

import cz.cvut.fel.bp.quizservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.quizservice.model.*;
import cz.cvut.fel.bp.quizservice.repository.QuizParticipantRepository;
import cz.cvut.fel.bp.quizservice.repository.QuizSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizSessionService {

    private static final String PIN_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    private final QuizSessionRepository sessionRepository;

    // TODO: refactor so only ParticipantService has access to ParticipantRepository
    private final QuizParticipantRepository participantRepository;

    @Transactional
    public QuizSession createQuizSession(QuizParticipant host) {
        return createQuizSession(host, List.of(), null);
    }

    @Transactional
    public QuizSession createQuizSession(QuizParticipant host, List<Long> deckIds, Map<Long, List<Long>> questionIds) {
        String pin = generateUniquePin();

        QuizSession session = QuizSession.builder()
                .lobbyPin(pin)
                .state(SessionState.LOBBY)
                .build();

        session.addParticipant(host);
        deckIds.forEach(deckId -> session.addDeck(deckId, questionIds.get(deckId)));

        log.info("action=createQuizSession hostId={} lobbyPin={}", host.getId(), pin);
        return sessionRepository.save(session);
    }

    @Transactional
    public void addDeckToQuizSession(String lobbyPin, Long deckId, List<Long> questionIds) {
        QuizSession session = findSessionByLobbyPin(lobbyPin);
        session.addDeck(deckId, questionIds);
        sessionRepository.save(session);
        log.info("action=addDeckToQuizSession lobbyPin={} deckId={}", lobbyPin, deckId);
    }

    @Transactional
    public void removeDeckFromQuizSession(String lobbyPin, Long deckId) {
        QuizSession session = findSessionByLobbyPin(lobbyPin);
        session.removeDeck(deckId);
        save(session);
        log.info("action=removeDeckFromQuizSession lobbyPin={} deckId={}", lobbyPin, deckId);
    }

    /**
     * Starts the game if the requesting user is the host
     *
     * @param pin the lobby pin of the session to start
     */
    @Transactional
    public void startSession(String pin) {
        QuizSession session = findSessionByLobbyPin(pin);

        session.setState(SessionState.QUIZ_STARTING);
        sessionRepository.save(session);

        log.info("action=startSession lobbyPin={} sessionState={}", pin, SessionState.QUIZ_STARTING);
    }

    @Transactional(readOnly = true)
    public QuizSession findSessionByLobbyPin(String pin) {
        return sessionRepository.findByLobbyPin(pin)
                .orElseThrow(() -> new ResourceNotFoundException("Session with pin " + pin + " not found"));
    }

    @Transactional
    public void save(QuizSession session) {
        sessionRepository.save(session);
    }

    @Transactional
    public void resetAnswersForSession(String pin) {
        participantRepository.resetAnswersForSession(pin);
    }

    @Transactional(readOnly = true)
    public boolean allConnectedParticipantsHaveAnswered(String pin) {
        QuizSession session = findSessionByLobbyPin(pin);

        return session.getParticipants().stream().anyMatch(QuizParticipant::getIsConnected)
                && session.getParticipants().stream()
                .filter(QuizParticipant::getIsConnected)
                .allMatch(participant -> participant.getIsCurrentCorrect() != null);
    }

    @Transactional(readOnly = true)
    public int getTotalQuestionCount(String pin) {
        QuizSession session = findSessionByLobbyPin(pin);
        return session.getSessionDecks().stream()
                .mapToInt(deck -> deck.getQuestionIds().size())
                .sum();
    }

    /**
     * Generates unique pin for the lobby.
     * Length of the pin is 6 characters, consisting of uppercase letters and digits.
     * @return unique pin
     */
    private String generateUniquePin() {
        String pin;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(PIN_CHARS.charAt(random.nextInt(PIN_CHARS.length())));
            }
            pin = sb.toString();
        } while (sessionRepository.existsByLobbyPin(pin));

        return pin;
    }

    @Transactional
    public void changeStateToQuestionResults(String pin) {
        QuizSession session = findSessionByLobbyPin(pin);
        session.setState(SessionState.QUESTION_RESULTS);
        save(session);
        log.info("action=changeStateToQuestionResults lobbyPin={} sessionState={}", pin, SessionState.QUESTION_RESULTS);
    }
}