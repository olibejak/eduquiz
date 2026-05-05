package cz.cvut.fel.bp.deckservice.controller;

import cz.cvut.fel.bp.deckservice.controller.util.RestUtils;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.facade.QuestionServiceFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing questions within decks.
 * Handles HTTP requests related to deck operations.
 */
@RestController
@RequestMapping("/api/decks/{deckId}/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {

    private final QuestionServiceFacade questionFacade;

    /**
     * Creates a new question in the specified deck with the given request data and requester ID.
     * @param deckId the ID of the deck to which the question will be added
     * @param request the question request data containing the question information
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @return the created question as a response DTO with HTTP 201 Created status and path header
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public ResponseEntity<QuestionResponseDTO> createQuestion(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long deckId,
            @Valid @RequestBody QuestionRequestDTO request) {
        log.info("Create question request userId={}, deckId={}", userPrincipal.id(), deckId);

        QuestionResponseDTO response = questionFacade.createQuestion(deckId, request, userPrincipal);
        log.info("Question created userId={}, deckId={}, questionId={}", userPrincipal.id(), deckId, response.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(RestUtils.createLocationHeaderFromCurrentUri("/{id}", response.id()))
                .body(response);
    }

    /**
     * Updates an existing question with the given question ID, request data, and requester ID.
     * @param id the ID of the question to update
     * @param deckId the ID of the deck to which the question belongs
     * @param request the question request data containing the updated question information
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @return the updated question as a response DTO
     */
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public QuestionResponseDTO updateQuestion(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long deckId,
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDTO request) {
        log.info("Update question request userId={}, deckId={}, questionId={}", userPrincipal.id(), deckId, id);

        return questionFacade.updateQuestion(id, deckId, request, userPrincipal);
    }

    /**
     * Deletes a question with the specified ID if the requester is authorized to do so.
     * @param id ID of the question
     * @param userPrincipal the authenticated user principal containing the requester's information
     */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id, @PathVariable Long deckId) {
        log.info("Delete question request userId={}, questionId={}", userPrincipal.id(), id);

        questionFacade.deleteQuestion(id, deckId, userPrincipal);
    }
}
