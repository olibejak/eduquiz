package cz.cvut.fel.bp.deckservice.service;

import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.exception.InvalidDeckOperationException;
import cz.cvut.fel.bp.deckservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.repository.QuestionRepository;
import cz.cvut.fel.bp.deckservice.service.validation.QuestionAnswerValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing Question entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionAnswerValidator questionAnswerValidator;

    /**
     * Retrieves a question by its ID.
     * @param questionId the ID of the question to retrieve
     * @return the retrieved question
     */
    @Transactional(readOnly = true)
    public Question getQuestionById(Long questionId) {
        log.debug("Load question questionId={}", questionId);
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
    }

    /**
     * Saves a question to the repository after validating its duration and answers.
     * @param question the question to save
     * @return the saved question
     */
    @Transactional
    public Question saveQuestion(Question question) {
        log.debug("Save question questionId={}, deckId={}", question.getId(),
                question.getDeck() != null ? question.getDeck().getId() : null);
        validateQuestionDuration(question.getDuration());
        questionAnswerValidator.validateQuestionAnswers(question);
        return questionRepository.save(question);
    }

    /**
     * Deletes a question from the repository.
     * @param question the question to delete
     */
    @Transactional
    public void deleteQuestion(Question question) {
        log.info("Delete question questionId={}", question.getId());
        questionRepository.delete(question);
    }

    /**
     * Retrieves a list of questions by their IDs.
     * @param questionIds the list of question IDs to retrieve
     * @return the list of retrieved questions
     */
    @Transactional(readOnly = true)
    public List<Question> getQuestionsByIds(List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return List.of();
        }
        return questionRepository.findAllByIdIn(questionIds);
    }

    /**
     * Validates that the question duration is a positive integer.
     * @param duration the duration to validate
     */
    private void validateQuestionDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            throw new InvalidDeckOperationException("Question duration must be a positive integer");
        }
    }

    public List<Question> getQuestionsByDeckId(Long deckId) {
        return questionRepository.findAllByDeckId(deckId);
    }

    public List<Long> getQuestionIdsByDeckId(Long deckId) {
        return questionRepository.findIdsByDeckId(deckId);
    }
}
