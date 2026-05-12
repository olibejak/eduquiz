package cz.cvut.fel.bp.userservice.listener;

import cz.cvut.fel.bp.userservice.dto.quiz.QuizEndedEventDTO;
import cz.cvut.fel.bp.userservice.service.QuizHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizHistoryRabbitListener {

    private final QuizHistoryService quizHistoryService;

    @RabbitListener(queues = "user-service.quiz-history.queue")
    @Transactional
    public void handleQuizEnded(QuizEndedEventDTO event) {
        log.info("Received QuizEndedEvent for session: {}", event.lobbyPin());
        try {
            quizHistoryService.handleQuizEndedEvent(event);
            log.info("Successfully saved quiz history for session: {}", event.lobbyPin());
        } catch (Exception e) {
            log.error("Failed to process quiz history for session: {}", event.lobbyPin(), e);
            throw e;
        }
    }
}