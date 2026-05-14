package cz.cvut.fel.bp.deckservice.service.facade;

import cz.cvut.fel.bp.deckservice.dto.question.BulkQuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.exception.InvalidDeckOperationException;
import cz.cvut.fel.bp.deckservice.mapper.facade.MapperFacade;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.security.UserPrincipal;
import cz.cvut.fel.bp.deckservice.service.DeckService;
import cz.cvut.fel.bp.deckservice.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A facade pattern for the QuestionController and the underlying services and mappers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceFacade {

    private final QuestionService questionService;
    private final DeckService deckService;
    private final MapperFacade mapperFacade;

    /**
     * Creates a new question in the specified deck with the given request data and requester ID.
     * @param deckId the ID of the deck to which the question will be added
     * @param request the question request data containing the question information
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @return dto of the created entity
     */
    @Transactional
    public QuestionResponseDTO createQuestion(Long deckId, QuestionRequestDTO request, UserPrincipal userPrincipal) {
        log.debug("Create question in facade userId={}, deckId={}", userPrincipal.id(), deckId);

        Deck deck = deckService.getDeckById(deckId);
        deckService.verifyOwnership(deck, userPrincipal);

        Question newQuestion = mapperFacade.toQuestionEntity(request);

        List<Answer> answers = request.answers().stream()
                .map(mapperFacade::toAnswerEntity)
                .toList();

        answers.forEach(answer -> answer.setQuestion(newQuestion));

        newQuestion.setAnswers(answers);
        deck.addQuestion(newQuestion);

        Question savedQuestion = questionService.saveQuestion(newQuestion);
        log.debug("Create question in facade completed userId={}, deckId={}, questionId={}",
                userPrincipal.id(), deckId, savedQuestion.getId());
        return mapperFacade.toQuestionResponse(savedQuestion);
    }

    /**
     * Updates an existing question with the given question ID, request data, and requester ID.
     * @param questionId the ID of the question to update
     * @param request the question request data containing the updated question information
     * @param userPrincipal the authenticated user principal containing the requester's information
     * @return dto of the updated entity
     */
    @Transactional
    public QuestionResponseDTO updateQuestion(
            Long questionId, Long deckId, QuestionRequestDTO request, UserPrincipal userPrincipal) {
        log.debug("Update question in facade userId={}, deckId={}, questionId={}",
                userPrincipal.id(), deckId, questionId);

        Question existingQuestion = questionService.getQuestionById(questionId);
        verifyQuestionOwnership(existingQuestion, userPrincipal);

        mapperFacade.updateQuestionFromRequest(request, existingQuestion);

        if (!existingQuestion.getDeck().getId().equals(deckId)) {
            log.warn("Question deck mismatch questionId={}, expectedDeckId={}, actualDeckId={}",
                    questionId, deckId, existingQuestion.getDeck().getId());
            throw new InvalidDeckOperationException("Question does not belong to the specified deck");
        }

        // ##########################################
        // TODO: Add dynamic updating
        // Not all answers have to be always deleted
        // Currently deleting all and replacing
        // ##########################################

        mapperFacade.replaceQuestionAnswers(request, existingQuestion);

        Question savedQuestion = questionService.saveQuestion(existingQuestion);
        log.debug("Update question in facade completed questionId={}, answersCount={}",
                savedQuestion.getId(), savedQuestion.getAnswers() != null ? savedQuestion.getAnswers().size() : 0);
        return mapperFacade.toQuestionResponse(savedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long deckId, UserPrincipal userPrincipal) {
        log.debug("Delete question in facade userId={}, questionId={}", userPrincipal.id(), questionId);
        Question question = questionService.getQuestionById(questionId);
        if (!Objects.equals(question.getDeck().getId(), deckId))
            throw new InvalidDeckOperationException("Question does not belong to the specified deck");
        verifyQuestionOwnership(question, userPrincipal);
        questionService.deleteQuestion(question);
    }

    /* Todo: Possible implementation of dynamic answer updating

    private void mergeAnswers(Question existingQuestion, List<AnswerRequestDTO> incomingDtos) {
        List<Answer> existingAnswers = existingQuestion.getAnswers();

        Set<Long> incomingIds = incomingDtos.stream()
                .map(AnswerRequestDTO::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // A) Delete
        existingAnswers.removeIf(existingAnswer ->
                existingAnswer.getId() != null && !incomingIds.contains(existingAnswer.getId())
        );

        // B) Add or update
        for (AnswerRequestDTO dto : incomingDtos) {
            if (dto.id() == null) {
                existingAnswers.add(answerMapper.toEntity(dto));
            } else {
                existingAnswers.stream()
                        .filter(a -> dto.id().equals(a.getId()))
                        .findFirst()
                        .ifPresent(existingAnswer -> {
                            Answer updated = answerMapper.updateEntityFromDTO(dto, existingAnswer);
                            int index = existingAnswers.indexOf(existingAnswer);
                            existingAnswers.set(index, updated);
                        });
            }
        }
    }
     */

    public void verifyQuestionOwnership(Question question, UserPrincipal userPrincipal) {
        log.debug("Verify question ownership userId={}, questionId={}", userPrincipal.id(), question.getId());
        deckService.verifyOwnership(question.getDeck(), userPrincipal);
    }

    public QuestionResponseDTO getQuestionById(Long questionId) {
        log.debug("Get question by id questionId={}", questionId);
        Question question = questionService.getQuestionById(questionId);
        return mapperFacade.toQuestionResponse(question);
    }

    public List<QuestionResponseDTO> getQuestionsByIds(List<Long> sessionIds) {
        log.debug("Get question for sessionIdsCount={}", sessionIds.size());
        List<Question> questions = questionService.getQuestionsByIds(sessionIds);
        return questions.stream().map(mapperFacade::toQuestionResponse).toList();
    }

    public List<Long> getQuestionIdsByDeckId(Long deckId) {
        return questionService.getQuestionIdsByDeckId(deckId);
    }

    @Transactional
    public List<QuestionResponseDTO> bulkUpdateQuestions(Long deckId, BulkQuestionRequestDTO bulkRequest, UserPrincipal userPrincipal) {
        Deck deck = deckService.getDeckById(deckId);
        deckService.verifyOwnership(deck, userPrincipal);

        List<Question> existingQuestions = deck.getQuestions();

        Set<Long> incomingIds = bulkRequest.updates().keySet();
        existingQuestions.removeIf(q -> !incomingIds.contains(q.getId()));

        bulkRequest.updates().forEach((questionId, questionDto) -> {
            Question existingQuestion = existingQuestions.stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Question with id: " + questionId + " is not part of the deck: " + deckId));

            mapperFacade.updateQuestionFromRequest(questionDto, existingQuestion);
            mapperFacade.replaceQuestionAnswers(questionDto, existingQuestion);
        });

        bulkRequest.creates().forEach(questionDto -> {
            Question newQuestion = mapperFacade.toQuestionEntity(questionDto);

            if (questionDto.answers() != null) {
                questionDto.answers().forEach(answerDto -> {
                    Answer answer = mapperFacade.toAnswerEntity(answerDto);
                    newQuestion.addAnswer(answer);
                });
            }

            deck.addQuestion(newQuestion);
        });

        Deck updatedDeck = deckService.saveDeck(deck);

        return updatedDeck.getQuestions().stream()
                .map(mapperFacade::toQuestionResponse)
                .collect(Collectors.toList());
    }
}
