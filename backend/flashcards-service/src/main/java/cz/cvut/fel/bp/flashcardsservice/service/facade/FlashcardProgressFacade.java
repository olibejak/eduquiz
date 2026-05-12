package cz.cvut.fel.bp.flashcardsservice.service.facade;

import cz.cvut.fel.bp.flashcardsservice.client.DeckServiceClient;
import cz.cvut.fel.bp.flashcardsservice.dto.*;
import cz.cvut.fel.bp.flashcardsservice.dto.deck.DeckProgressStatusDTO;
import cz.cvut.fel.bp.flashcardsservice.dto.deck.DeckProgressSummaryDTO;
import cz.cvut.fel.bp.flashcardsservice.exception.DeckNotFoundException;
import cz.cvut.fel.bp.flashcardsservice.exception.FlashcardReviewException;
import cz.cvut.fel.bp.flashcardsservice.exception.InvalidFlashcardRatingException;
import cz.cvut.fel.bp.flashcardsservice.exception.StudySessionException;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardRating;
import cz.cvut.fel.bp.flashcardsservice.model.FlashcardProgress;
import cz.cvut.fel.bp.flashcardsservice.security.UserPrincipal;
import cz.cvut.fel.bp.flashcardsservice.service.FlashcardProgressService;
import cz.cvut.fel.bp.flashcardsservice.service.spacedRepetition.SpacedRepetitionCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressFacade {

    private final FlashcardProgressService flashcardProgressService;
    private final SpacedRepetitionCalculator spacedRepetitionCalculator;
    private final DeckServiceClient deckServiceClient;

    /**
     * Submits a batch of flashcard reviews for a user.
     * @param userId the ID of the user submitting reviews
     * @param deckId the ID of the deck being reviewed
     * @param batch the batch of reviews to submit
     * @throws DeckNotFoundException if the deck does not exist
     * @throws FlashcardReviewException if review processing fails
     * @throws InvalidFlashcardRatingException if an invalid rating is encountered
     */
    @Transactional
    public void submitCardReviewBatch(UUID userId, Long deckId, FlashcardReviewBatchDTO batch) {
        try {
            log.debug("Validating deck existence userId={}, deckId={}", userId, deckId);
            validateDeckExists(deckId);

            List<Long> questionIds = batch.reviews().stream()
                    .map(FlashcardReviewDTO::questionId)
                    .toList();

            log.debug("Retrieving progress map userId={}, deckId={}, questionCount={}", userId, deckId, questionIds.size());
            Map<Long, FlashcardProgress> progressMap = getQuestionIdToProgressMap(userId, deckId, questionIds);

            List<FlashcardProgress> toSave = new ArrayList<>();

            for (FlashcardReviewDTO review : batch.reviews()) {
                log.debug("Validating flashcard rating userId={}, deckId={}, questionId={}, rating={}",
                        userId, deckId, review.questionId(), review.rating());
                validateFlashcardRating(review.rating());

                FlashcardProgress progress = progressMap.getOrDefault(
                        review.questionId(),
                        FlashcardProgress.builder()
                                .deckId(deckId)
                                .questionId(review.questionId())
                                .userId(userId)
                                .build()
                );
                applySpacedRepetitionMath(progress, review.rating());
                toSave.add(progress);
            }

            flashcardProgressService.saveProgress(toSave);
            log.info("Flashcard reviews submitted userId={}, deckId={}, reviewCount={}", userId, deckId, toSave.size());
        } catch (DeckNotFoundException | InvalidFlashcardRatingException e) {
            log.warn("Flashcard review submission failed userId={}, deckId={}, reason={}", userId, deckId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error submitting flashcard review batch userId={}, deckId={}", userId, deckId, e);
            throw new FlashcardReviewException("Failed to process flashcard review batch: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the progress status for a specific deck.
     * @param userId the ID of the user
     * @param deckId the ID of the deck
     * @return the deck progress status
     * @throws DeckNotFoundException if the deck does not exist or has no questions
     */
    @Transactional(readOnly = true)
    public DeckProgressStatusDTO getDeckStatus(UUID userId, Long deckId) {
        try {
            log.debug("Retrieving deck status userId={}, deckId={}", userId, deckId);
            validateDeckExists(deckId);

            Integer due = flashcardProgressService.countDueCardsForDeckForUser(userId, deckId);
            Integer started = flashcardProgressService.countByUserIdAndDeckId(userId, deckId);
            Integer totalQuestionsCount = getTotalQuestionsOrThrow(deckId);
            Integer newCards = totalQuestionsCount - started;

            log.debug("Deck status retrieved userId={}, deckId={}, due={}, started={}, totalQuestions={}",
                    userId, deckId, due, started, totalQuestionsCount);

            return DeckProgressStatusDTO.builder()
                    .dueCount(due)
                    .newCount(newCards)
                    .learnedCount(started)
                    .totalCount(totalQuestionsCount)
                    .build();
        } catch (DeckNotFoundException e) {
            log.warn("Deck not found, creating default entry userId={}, deckId={}", userId, deckId);
            return createDefaultDeckStatus(deckId);
        } catch (Exception e) {
            log.error("Error retrieving deck status userId={}, deckId={}", userId, deckId, e);
            return createDefaultDeckStatus(deckId);
        }
    }

    /**
     * Generates a study session with questions for the user.
     * @param userId the ID of the user
     * @param deckId the ID of the deck
     * @param sessionSize the desired session size
     * @return list of flashcard DTOs for the session
     * @throws DeckNotFoundException if the deck does not exist
     * @throws StudySessionException if session generation fails
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getStudySession(UUID userId, Long deckId, int sessionSize) {
        try {
            log.debug("Generating study session userId={}, deckId={}, sessionSize={}", userId, deckId, sessionSize);
            validateDeckExists(deckId);

            List<Long> allQuestionIds = deckServiceClient.getAllQuestionIdsForDeck(deckId);
            log.debug("Retrieved all question IDs userId={}, deckId={}, questionCount={}", userId, deckId, allQuestionIds.size());

            if (allQuestionIds.isEmpty()) {
                log.warn("No questions available in deck userId={}, deckId={}", userId, deckId);
                throw new StudySessionException("No questions available in deck " + deckId);
            }

            List<Long> sessionIds = flashcardProgressService.generateSessionQuestionIds(
                    userId, deckId, sessionSize, allQuestionIds
            );
            log.debug("Session IDs generated userId={}, deckId={}, sessionSize={}, generatedCount={}",
                    userId, deckId, sessionSize, sessionIds.size());

            if (sessionIds.isEmpty()) {
                log.warn("Failed to generate study session userId={}, deckId={}, sessionSize={}", userId, deckId, sessionSize);
                throw new StudySessionException("Unable to generate study session for deck " + deckId);
            }

            List<QuestionDTO> result = deckServiceClient.getQuestionsDetailsByIds(sessionIds);
            log.info("Study session created userId={}, deckId={}, cardCount={}", userId, deckId, result.size());
            return result;
        } catch (DeckNotFoundException | StudySessionException e) {
            log.warn("Study session generation failed userId={}, deckId={}, reason={}", userId, deckId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error generating study session userId={}, deckId={}, sessionSize={}", userId, deckId, sessionSize, e);
            throw new StudySessionException("Failed to generate study session: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a paginated list of decks with due cards for the user's dashboard.
     * @param userId the ID of the user
     * @param pageable pagination information
     * @return a slice of deck progress summaries
     */
    @Transactional(readOnly = true)
    public Slice<DeckProgressSummaryDTO> getDueDecksDashboard(UUID userId, Pageable pageable) {
        log.debug("Retrieving due decks dashboard userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        Slice<Long> dueDeckIds = flashcardProgressService.getPriorityDeckIdsForUser(userId, pageable);
        log.debug("Retrieved due deck IDs userId={}, deckCount={}", userId, dueDeckIds.getNumberOfElements());

        Map<Long, String> deckNames = dueDeckIds.isEmpty() ? Map.of()
                : deckServiceClient.getDeckNames(dueDeckIds.getContent());

        Slice<DeckProgressSummaryDTO> result = dueDeckIds.map(deckId -> DeckProgressSummaryDTO.builder()
                .id(deckId)
                .title(deckNames.getOrDefault(deckId, "Deck " + deckId))
                .build());

        log.debug("Due decks dashboard constructed userId={}, deckCount={}", userId, result.getNumberOfElements());
        return result;
    }

    private Map<Long, FlashcardProgress> getQuestionIdToProgressMap(UUID userId, Long deckId, List<Long> questionIds) {
        List<FlashcardProgress> retrievedProgress = flashcardProgressService.getProgress(userId, deckId, questionIds);

        return retrievedProgress.stream()
                .collect(java.util.stream.Collectors.toMap(
                        FlashcardProgress::getQuestionId, progress -> progress
                ));

    }

    private void applySpacedRepetitionMath(FlashcardProgress progress, FlashcardRating rating) {
        LocalDateTime now = LocalDateTime.now();

        int newIntervalDays = spacedRepetitionCalculator.calculateNextInterval(rating, progress.getIntervalDays());
        LocalDateTime newNextReview = spacedRepetitionCalculator.calculateNextReview(newIntervalDays);

        progress.setLastAnsweredAt(now);
        progress.setUserRating(rating);
        progress.setIntervalDays(newIntervalDays);
        progress.setNextReviewAt(newNextReview);
    }

    /**
     * Validates that a deck exists by checking with the deck service.
     * @param deckId the ID of the deck to validate
     * @throws DeckNotFoundException if the deck does not exist
     */
    private void validateDeckExists(Long deckId) {
        try {
            log.debug("Validating deck existence deckId={}", deckId);
            String deckName = deckServiceClient.getDeckName(deckId);
            if (deckName == null || deckName.isBlank()) {
                log.warn("Deck validation failed deckId={}, reason=nameNotFound", deckId);
                throw new DeckNotFoundException(deckId);
            }
            log.debug("Deck validation succeeded deckId={}", deckId);
        } catch (DeckNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Deck validation error deckId={}, reason={}", deckId, e.getMessage());
            throw new DeckNotFoundException(deckId);
        }
    }

    /**
     * Gets the total number of questions in a deck, throwing if none exist.
     * @param deckId the ID of the deck
     * @return the total question count
     * @throws DeckNotFoundException if the deck has no questions
     */
    private Integer getTotalQuestionsOrThrow(Long deckId) {
        log.debug("Retrieving total question count deckId={}", deckId);
        Integer totalQuestionsCount = deckServiceClient.getTotalQuestionsCount(deckId);
        if (totalQuestionsCount == null || totalQuestionsCount <= 0) {
            log.warn("Deck has no questions deckId={}, count={}", deckId, totalQuestionsCount);
            throw new DeckNotFoundException("Deck with ID " + deckId + " not found or has no questions");
        }
        log.debug("Total question count retrieved deckId={}, count={}", deckId, totalQuestionsCount);
        return totalQuestionsCount;
    }

    /**
     * Validates that a flashcard rating is supported.
     * @param rating the rating to validate
     * @throws InvalidFlashcardRatingException if the rating is not supported
     */
    private void validateFlashcardRating(FlashcardRating rating) {
        if (rating == null || !isValidRating(rating)) {
            log.warn("Invalid flashcard rating provided rating={}", rating);
            throw new InvalidFlashcardRatingException("Invalid flashcard rating: " + rating);
        }
    }

    private boolean isValidRating(FlashcardRating rating) {
        return rating == FlashcardRating.AGAIN ||
               rating == FlashcardRating.HARD ||
               rating == FlashcardRating.GOOD ||
               rating == FlashcardRating.EXCELLENT;
    }

    /**
     * Creates a default deck status entry with zero values when deck is not found.
     * @param deckId the ID of the deck
     * @return a default DeckProgressStatusDTO with all counts set to 0
     */
    private DeckProgressStatusDTO createDefaultDeckStatus(Long deckId) {
        log.info("Creating default deck status entry deckId={}", deckId);
        return DeckProgressStatusDTO.builder()
                .dueCount(0)
                .newCount(0)
                .learnedCount(0)
                .totalCount(0)
                .build();
    }

    public void deleteDeckProgress(UserPrincipal userPrincipal, Long deckId) {
        log.debug("Deleting deck progress userId={}, deckId={}", userPrincipal.id(), deckId);
        flashcardProgressService.deleteDeckProgress(userPrincipal.id(), deckId);
        log.info("Deck progress deleted userId={}, deckId={}", userPrincipal.id(), deckId);
    }
}
