package cz.cvut.fel.bp.quizservice.service.util;


import cz.cvut.fel.bp.quizservice.service.facade.QuizGameFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionTimer {

    private final TaskScheduler taskScheduler;

    @Lazy
    private final QuizGameFacade quizGameFacade;

    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();

    public void scheduleTimeout(String pin, int seconds) {
        cancelTimer(pin);

        log.debug("action=scheduleTimeout lobbyPin={} seconds={}", pin, seconds);

        ScheduledFuture<?> task = taskScheduler.schedule(
                () -> {
                    log.debug("action=timeoutQuestionTriggered lobbyPin={}", pin);
                    quizGameFacade.endQuestion(pin);
                    activeTimers.remove(pin);
                },
                Instant.now().plusSeconds(seconds)
        );
        activeTimers.put(pin, task);
    }

    public void cancelTimer(String pin) {
        boolean cancelled = Optional.ofNullable(activeTimers.remove(pin)).map(t -> {
            t.cancel(false);
            return true;
        }).orElse(false);
        log.debug("action=cancelTimer lobbyPin={} result={}", pin, cancelled ? "cancelled" : "notFound");
    }
}
