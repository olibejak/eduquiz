package cz.cvut.fel.bp.quizservice.service;

import cz.cvut.fel.bp.quizservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.quizservice.model.QuizSession;
import cz.cvut.fel.bp.quizservice.model.SessionState;
import cz.cvut.fel.bp.quizservice.repository.QuizSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.session;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizSessionServiceTest {

    @Mock
    private QuizSessionRepository sessionRepository;

    @Mock
    private cz.cvut.fel.bp.quizservice.repository.QuizParticipantRepository participantRepository;

    private QuizSessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new QuizSessionService(sessionRepository, participantRepository);
    }

    @Test
    void createQuizSessionGeneratesLobby() {
        var host = cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant(
                1L, null, UUID.randomUUID(), "Host", null, "token-1", 0, true);
        when(sessionRepository.existsByLobbyPin(anyString())).thenReturn(true, false);
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizSession session = sessionService.createQuizSession(host);
        String pin = session.getLobbyPin();

        assertThat(pin.length()).isEqualTo(6);
        verify(sessionRepository).save(any());
        assertThat(session.getState()).isEqualTo(SessionState.LOBBY);
        assertThat(session.getParticipants()).containsExactly(host);
        verify(sessionRepository, times(2)).existsByLobbyPin(anyString());
    }

    @Test
    void addDeckToQuizSessionAddsDeck() {
        QuizSession session = session("PIN123", UUID.fromString("11111111-1111-1111-1111-111111111111"), SessionState.LOBBY);
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sessionService.addDeckToQuizSession("PIN123", 10L, List.of(100L, 101L));

        assertThat(session.getSessionDecks()).hasSize(1);
        assertThat(session.getSessionDecks().getFirst().getDeckId()).isEqualTo(10L);
        assertThat(session.getSessionDecks().getFirst().getPlayOrder()).isEqualTo(0);
        assertThat(session.getSessionDecks().getFirst().getQuestionIds()).containsExactly(100L, 101L);
    }

    @Test
    void removeDeckFromQuizSessionRemovesDeckAndReindexes() {
        QuizSession session = session("PIN123", UUID.fromString("11111111-1111-1111-1111-111111111111"), SessionState.LOBBY);
        session.addDeck(10L, List.of(100L));
        session.addDeck(20L, List.of(200L));
        session.addDeck(30L, List.of(300L));
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sessionService.removeDeckFromQuizSession("PIN123", 20L);

        assertThat(session.getSessionDecks()).hasSize(2);
        assertThat(session.getSessionDecks().get(0).getDeckId()).isEqualTo(10L);
        assertThat(session.getSessionDecks().get(0).getPlayOrder()).isEqualTo(0);
        assertThat(session.getSessionDecks().get(1).getDeckId()).isEqualTo(30L);
        assertThat(session.getSessionDecks().get(1).getPlayOrder()).isEqualTo(1);
    }

    @Test
    void startSessionChangesState() {
        QuizSession session = session("PIN123", UUID.fromString("11111111-1111-1111-1111-111111111111"), SessionState.LOBBY);
        var host = cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant(
                1L, session, UUID.randomUUID(), "Host", null, "token-1", 0, true);
        session.addParticipant(host);
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sessionService.startSession("PIN123", true);

        assertThat(session.getState()).isEqualTo(SessionState.QUIZ_STARTING);
        verify(sessionRepository).save(session);
    }

    @Test
    void findSessionByLobbyPinThrowsWhenMissing() {
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.findSessionByLobbyPin("PIN123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeStateToQuestionResultsUpdatesState() {
        QuizSession session = session("PIN123", UUID.fromString("11111111-1111-1111-1111-111111111111"), SessionState.QUESTION_ACTIVE);
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sessionService.changeStateToQuestionResults("PIN123");

        assertThat(session.getState()).isEqualTo(SessionState.QUESTION_RESULTS);
        verify(sessionRepository).save(session);
    }

    @Test
    void allConnectedParticipantsHaveAnsweredReturnsFalseWhenMissing() {
        QuizSession session = session("PIN123", UUID.fromString("11111111-1111-1111-1111-111111111111"), SessionState.QUESTION_ACTIVE);
        var p1 = cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant(1L, session, UUID.randomUUID(), "Alice", null, "token-1", 0, true);
        p1.setIsCurrentCorrect(true);
        var p2 = cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant(2L, session, UUID.randomUUID(), "Bob", null, "token-2", 0, true);
        p2.setIsCurrentCorrect(null);
        session.addParticipant(p1);
        session.addParticipant(p2);
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.of(session));

        assertThat(sessionService.allConnectedParticipantsHaveAnswered("PIN123")).isFalse();
    }

    @Test
    void getTotalQuestionCountSumsAllDecks() {
        QuizSession session = session("PIN123", UUID.fromString("11111111-1111-1111-1111-111111111111"), SessionState.LOBBY);
        session.addDeck(10L, List.of(100L, 101L));
        session.addDeck(20L, List.of(200L, 201L, 202L));
        when(sessionRepository.findByLobbyPin("PIN123")).thenReturn(Optional.of(session));

        int count = sessionService.getTotalQuestionCount("PIN123");

        assertThat(count).isEqualTo(5);
    }

}


