package cz.cvut.fel.bp.flashcardsservice.service;

import cz.cvut.fel.bp.flashcardsservice.model.FlashcardProgress;
import cz.cvut.fel.bp.flashcardsservice.repository.FlashcardsProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardProgressService {

    private final FlashcardsProgressRepository flashcardsProgressRepository;

    /**
     * Retrieves the flashcard progress for a specific user and question.
     * @param userId ID of the user for whom to retrieve
     * @param questionIds List of IDs of the questions for which to retrieve
     * @return The existing FlashcardsProgress for the specified user and question
     */
    @Transactional(readOnly = true)
    public List<FlashcardProgress> getProgress(UUID userId, Long deckId, List<Long> questionIds) {
        log.debug("Retrieving progress userId={}, deckId={}, questionCount={}", userId, deckId, questionIds.size());
        List<FlashcardProgress> result = flashcardsProgressRepository.findAllExistingProgress(userId, deckId, questionIds);
        log.debug("Progress retrieved userId={}, deckId={}, progressCount={}", userId, deckId, result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public List<Long> generateSessionQuestionIds(UUID userId, Long deckId, int sessionSize, List<Long> allDeckQuestionIds) {
        log.debug("Generating session question IDs userId={}, deckId={}, sessionSize={}, totalQuestions={}",
                userId, deckId, sessionSize, allDeckQuestionIds.size());

        List<Long> dueQuestionIds = flashcardsProgressRepository.findDueQuestionIds(
                userId, deckId, LocalDateTime.now(), PageRequest.of(0, sessionSize)
        );
        log.debug("Due questions found userId={}, deckId={}, dueCount={}", userId, deckId, dueQuestionIds.size());

        List<Long> sessionIds = new ArrayList<>(dueQuestionIds);

        if (sessionIds.size() < sessionSize) {
            Set<Long> seenIds = flashcardsProgressRepository.findAllSeenQuestionIds(userId, deckId);
            int neededNewCards = sessionSize - sessionIds.size();
            List<Long> newIds = allDeckQuestionIds.stream()
                    .filter(id -> !seenIds.contains(id))
                    .limit(neededNewCards)
                    .toList();
            sessionIds.addAll(newIds);
            log.debug("New questions added userId={}, deckId={}, newCount={}, totalSession={}",
                    userId, deckId, newIds.size(), sessionIds.size());
        }

        if (sessionIds.isEmpty()) {
            fillWithExtraQuestions(sessionIds, allDeckQuestionIds, sessionSize);
            log.debug("Extra practice questions added userId={}, deckId={}, finalSessionSize={}",
                    userId, deckId, sessionIds.size());
        }

        Collections.shuffle(sessionIds);
        log.debug("Session question IDs generated userId={}, deckId={}, finalCount={}", userId, deckId, sessionIds.size());
        return sessionIds;
    }

    private void fillWithExtraQuestions(List<Long> currentSessionIds, List<Long> allDeckQuestionIds, int targetSize) {
        int neededExtraCards = targetSize - currentSessionIds.size();

        List<Long> availableForExtra = new ArrayList<>(allDeckQuestionIds);
        availableForExtra.removeAll(currentSessionIds);

        Collections.shuffle(availableForExtra);

        List<Long> extraIds = availableForExtra.stream()
                .limit(neededExtraCards)
                .toList();

        currentSessionIds.addAll(extraIds);
    }

    @Transactional
    public void saveProgress(List<FlashcardProgress> progress) {
        log.debug("Saving progress records count={}", progress.size());
        flashcardsProgressRepository.saveAll(progress);
        log.debug("Progress records saved count={}", progress.size());
    }

    @Transactional(readOnly = true)
    public Integer countDueCardsForDeckForUser(UUID userId, Long deckId) {
        log.debug("Counting due cards userId={}, deckId={}", userId, deckId);
        LocalDateTime now = LocalDateTime.now();
        Integer count = flashcardsProgressRepository.countDueCardsForDeck(userId, deckId, now);
        log.debug("Due cards counted userId={}, deckId={}, count={}", userId, deckId, count);
        return count;
    }

    @Transactional(readOnly = true)
    public Integer countByUserIdAndDeckId(UUID userId, Long deckId) {
        log.debug("Counting started cards userId={}, deckId={}", userId, deckId);
        Integer count = flashcardsProgressRepository.countByUserIdAndDeckId(userId, deckId);
        log.debug("Started cards counted userId={}, deckId={}, count={}", userId, deckId, count);
        return count;
    }

    @Transactional(readOnly = true)
    public Slice<Long> getPriorityDeckIdsForUser(UUID userId, Pageable pageable) {
        log.debug("Retrieving priority deck IDs userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        LocalDateTime now = LocalDateTime.now();
        Slice<Long> result = flashcardsProgressRepository.findPriorityDeckIds(userId, now, pageable);
        log.debug("Priority deck IDs retrieved userId={}, count={}, hasNext={}",
                userId, result.getNumberOfElements(), result.hasNext());
        return result;
    }

    @Transactional
    public void deleteDeckProgress(UUID id, Long deckId) {
        log.debug("Deleting deck progress userId={}, deckId={}", id, deckId);
        flashcardsProgressRepository.deleteByUserIdAndDeckId(id, deckId);
        log.debug("Deck progress deleted userId={}, deckId={}", id, deckId);
    }
}
