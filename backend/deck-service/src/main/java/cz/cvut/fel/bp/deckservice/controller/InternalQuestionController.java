package cz.cvut.fel.bp.deckservice.controller;

import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.service.facade.QuestionServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalQuestionController {

    private final QuestionServiceFacade questionServiceFacade;

    @GetMapping("/questions/{id}")
    public QuestionResponseDTO getQuestionDetailsById(@PathVariable("id") Long sessionId) {
        return questionServiceFacade.getQuestionById(sessionId);
    }

    @GetMapping("/questions")
    public List<QuestionResponseDTO> getQuestionsDetailsByIds(@RequestParam("ids") List<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return List.of();
        return questionServiceFacade.getQuestionsByIds(sessionIds);
    }

    @GetMapping("/decks/{id}/questions/ids")
    public List<Long> getQuestionsIdsByDeckId(@PathVariable("id") Long deckId) {
        return questionServiceFacade.getQuestionIdsByDeckId(deckId);
    }
}
