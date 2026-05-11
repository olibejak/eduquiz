package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.dto.quiz.QuizQuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.results.QuestionResultsDTO;
import cz.cvut.fel.bp.quizservice.service.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisherFacade {

    private final ApplicationEventPublisher publisher;

    /**
     * Generic publish method that can be used for any application event.
     */
    public void publish(Object event) {
        log.debug("action=publishEvent event={}", event.getClass().getSimpleName());
        publisher.publishEvent(event);
    }

    public void publishLobbyCreated(String lobbyPin, Long hostParticipantId) {
        publish(new LobbyCreatedEvent(lobbyPin, hostParticipantId));
    }

    public void publishSessionDeckChanged(String lobbyPin, Long deckId, SessionDeckChangedEvent.ChangeType changeType) {
        publish(new SessionDeckChangedEvent(lobbyPin, deckId, changeType));
    }

    public void publishParticipantConnectionChanged(String lobbyPin, Long participantId, String nickname,
                                                    ParticipantConnectionChangedEvent.Status status) {
        publish(new ParticipantConnectionChangedEvent(lobbyPin, participantId, nickname, status));
    }

    public void publishQuestionEnded(String lobbyPin, QuestionResultsDTO results) {
        publish(new QuestionEndedEvent(lobbyPin, results));
    }

    public void publishQuestionStarted(String lobbyPin, QuizQuestionDTO question) {
        publish(new QuestionStartedEvent(lobbyPin, question));
    }

    public void publishQuizEnded(String lobbyPin) {
        publish(new QuizEndedEvent(lobbyPin));
    }

    public void publishQuizStarted(String lobbyPin, int totalQuestions) {
        publish(new QuizStartedEvent(lobbyPin, totalQuestions));
    }

    public void publishHostTransferEvent(String lobbyPin, Long oldHostId, Long newHostId) {
        publish(new HostTransferEvent(lobbyPin, oldHostId, newHostId));
    }
}

