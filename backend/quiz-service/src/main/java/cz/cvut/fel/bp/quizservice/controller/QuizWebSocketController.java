package cz.cvut.fel.bp.quizservice.controller;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.service.facade.QuizGameFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QuizWebSocketController {

    private final QuizGameFacade quizGameFacade;

    @MessageMapping("/quiz/{pin}/answer")
    public void submitAnswer(
            @DestinationVariable String pin,
            @Payload AnswerSubmitDTO answerDTO,
            Principal principal
    ) {

        Long participantId = Long.valueOf(principal.getName());
        log.info("Principal ID={}, Participant ID={}", participantId, answerDTO.participantId());
        log.debug("action=submitAnswer lobbyPin={} participantId={} questionId={} answerType={}", answerDTO.lobbyPin(), participantId, answerDTO.questionId(), answerDTO.answerType());
        quizGameFacade.processAnswer(answerDTO);
    }

    @MessageMapping("/quiz/{pin}/next")
    public void nextQuestion(@DestinationVariable String pin) {
        log.debug("Učitel posouvá hru {} na další otázku", pin);
        quizGameFacade.moveToNextQuestion(pin);
    }
}
