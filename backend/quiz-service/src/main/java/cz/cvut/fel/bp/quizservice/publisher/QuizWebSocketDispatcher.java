package cz.cvut.fel.bp.quizservice.publisher;

import cz.cvut.fel.bp.quizservice.service.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizWebSocketDispatcher {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHostChange(HostTransferEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/host", event);
    }

    // LOBBY
    @EventListener
    public void onPresenceChanged(ParticipantConnectionChangedEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/presence", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeckChanged(SessionDeckChangedEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/deck", event);
    }

    // QUIZ
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGameStarted(QuizStartedEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/start", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuestionStarted(QuestionStartedEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/question", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuestionFinished(QuestionEndedEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/question-results", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuizFinished(QuizEndedEvent event) {
        messagingTemplate.convertAndSend("/topic/quiz/" + event.lobbyPin() + "/finished", event);
    }
}
