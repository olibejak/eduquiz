package cz.cvut.fel.bp.quizservice.controller;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.service.facade.QuizGameFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QuizWebSocketController {

    private final QuizGameFacade quizGameFacade;

    @PreAuthorize("hasRole('USER')")
    @MessageMapping("/quiz/{pin}/answer")
    public void submitAnswer(
            @DestinationVariable String pin,
            @Payload AnswerSubmitDTO answerDTO) {

        log.debug("action=submitAnswer lobbyPin={} participantId={} questionId={} answerType={}", answerDTO.lobbyPin(), answerDTO.participantId(), answerDTO.questionId(), answerDTO.answerType());
        quizGameFacade.processAnswer(answerDTO);
    }

    @PreAuthorize("hasRole('HOST')")
    @MessageMapping("/quiz/{pin}/next")
    public void nextQuestion(@DestinationVariable String pin) {
        log.debug("Učitel posouvá hru {} na další otázku", pin);
        quizGameFacade.moveToNextQuestion(pin);
    }
}
