package cz.cvut.fel.bp.quizservice.service;

import cz.cvut.fel.bp.quizservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.model.SessionState;
import cz.cvut.fel.bp.quizservice.repository.QuizParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.session;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizParticipantServiceTest {

    @Mock
    private QuizParticipantRepository participantRepository;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    private QuizParticipantService participantService;

    @BeforeEach
    void setUp() {
        participantService = new QuizParticipantService(participantRepository);
    }

    @Test
    void findByIdReturnsParticipant() {
        QuizParticipant player = participant(1L, session("PIN123", UUID.randomUUID(), SessionState.LOBBY), UUID.randomUUID(), "Alice", "device-1", "token-1", 0, false);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(player));

        QuizParticipant result = participantService.findById(1L);

        assertThat(result).isSameAs(player);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(participantRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participantService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changeConnectionStatusUpdatesParticipantAndSavesIt() {
        QuizParticipant player = participant(1L, session("PIN123", UUID.randomUUID(), SessionState.LOBBY), UUID.randomUUID(), "Alice", "device-1", "token-1", 0, false);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(player));
        when(participantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizParticipant result = participantService.changeConnectionStatus(1L, true);

        assertThat(result.getIsConnected()).isTrue();
        verify(participantRepository).save(player);
    }

    @Test
    void addPointsMutatesParticipantWithoutExplicitSave() {
        QuizParticipant player = participant(1L, session("PIN123", UUID.randomUUID(), SessionState.LOBBY), UUID.randomUUID(), "Alice", "device-1", "token-1", 2, false);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(player));

        QuizParticipant result = participantService.evaluate(1L, 3, true);

        assertThat(result.getCurrentScore()).isEqualTo(5);
        verify(participantRepository, never()).save(any());
    }

    @Test
    void addPointsAndAdvanceUpdatesCurrentScoreAndSaves() {
        QuizParticipant player = participant(1L, session("PIN123", UUID.randomUUID(), SessionState.LOBBY), UUID.randomUUID(), "Alice", "device-1", "token-1", 2, false);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(player));
        when(participantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        participantService.addPointsAndAdvance(1L, 3);

        assertThat(player.getCurrentScore()).isEqualTo(5);
        verify(participantRepository).save(player);
    }
}


