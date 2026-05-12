package cz.cvut.fel.bp.deckservice.mapper.facade;

import cz.cvut.fel.bp.deckservice.client.UserServiceClient;
import cz.cvut.fel.bp.deckservice.dto.answer.AnswerRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckDetailsResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.deck.DeckSummaryResponseDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionRequestDTO;
import cz.cvut.fel.bp.deckservice.dto.question.QuestionResponseDTO;
import cz.cvut.fel.bp.deckservice.mapper.AnswerMapperImpl;
import cz.cvut.fel.bp.deckservice.mapper.DeckMapper;
import cz.cvut.fel.bp.deckservice.mapper.QuestionMapper;
import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.ChoiceAnswer;
import cz.cvut.fel.bp.deckservice.model.Deck;
import cz.cvut.fel.bp.deckservice.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperFacade {
    private final QuestionMapper questionMapper;
    private final DeckMapper deckMapper;
    private final AnswerMapperImpl answerMapper;
    private final UserServiceClient userServiceClient;

    /**
     * Converts a DeckRequestDTO into a Deck entity, including mapping nested questions and answers.
     * @param request the DeckRequestDTO containing the deck information and nested questions and answers to be mapped
     * @return a Deck entity corresponding to the given DeckRequestDTO, with nested questions and answers properly mapped
     */
    public Deck toDeckEntity(DeckRequestDTO request) {
        Deck deckToCreate = deckMapper.deckRequestToDeck(request);

        if (request.questions() == null) {
            return deckToCreate;
        }

        request.questions().forEach(questionDto -> {
            Question question = questionMapper.toEntity(questionDto);

            if (questionDto.answers() != null) {
                questionDto.answers().forEach(answerDto -> {
                    Answer answer = answerMapper.toEntity(answerDto);
                    question.addAnswer(answer);
                });
            }

            deckToCreate.addQuestion(question);
        });

        return deckToCreate;
    }

    public Answer toAnswerEntity(AnswerRequestDTO request) {
        return answerMapper.toEntity(request);
    }

    public DeckDetailsResponseDTO toDeckDetailsResponse(Deck deck) {
        return deckMapper.deckToDetailsResponse(deck);
    }

    public void updateDeckFromRequest(DeckRequestDTO request, Deck deck) {
        deckMapper.updateDeckFromRequest(request, deck);
    }

    public Question toQuestionEntity(QuestionRequestDTO request) {
        return questionMapper.toEntity(request);
    }

    public void updateQuestionFromRequest(QuestionRequestDTO request, Question question) {
        questionMapper.updateEntityFromDTO(request, question);
    }

    public QuestionResponseDTO toQuestionResponse(Question question) {
        return questionMapper.toResponse(question);
    }

    public void replaceQuestionAnswers(QuestionRequestDTO request, Question question) {
        question.getAnswers().clear();
        request.answers().forEach(answerDto -> question.addAnswer(answerMapper.toEntity(answerDto)));
    }

    /**
     * Fetches the user IDs of users whose names match the given author name by calling the UserServiceClient.
     * @param authorName the author name to search for matching user IDs
     * @return a list of user IDs corresponding to users whose names match the given author name
     */
    public List<UUID> getUserIdsByAuthorName(String authorName) {
        List<UUID> matchingUserIds = userServiceClient.getUserIdsByName(authorName);
        log.debug("Search decks in facade authorName matchCount={}", matchingUserIds.size());
        return matchingUserIds;
    }

    /**
     * Converts a page of deck entities into a page of deck summary response DTOs, including author names.
     * @param entityPage the page of deck entities to convert into summary response DTOs
     * @return a page of deck summary response DTOs corresponding to the given page of deck entities
     */
    public Slice<DeckSummaryResponseDTO> toDeckSummarySlice(Slice<Deck> entityPage) {
        if (!entityPage.hasContent()) {
            log.debug("Map deck summaries skipped because result is empty");
            return new SliceImpl<>(List.of(), entityPage.getPageable(), false);
        }

        Map<UUID, String> authorNamesMap = mapAuthorIdsToUsernames(entityPage);
        return entityPage.map(deck -> {
            String authorName = authorNamesMap.getOrDefault(deck.getAuthorId(), "Unknown User");
            return deckMapper.deckToSummaryResponse(deck, authorName);
        });
    }

    /**
     * Fetches the usernames for the authors of the decks in the given page and maps them by author ID.
     * @param entityPage the page of deck entities for which to fetch author usernames
     * @return a map of author IDs to their corresponding usernames for the authors of the decks in the page
     */
    private Map<UUID, String> mapAuthorIdsToUsernames(Slice<Deck> entityPage) {
        Set<UUID> authorIds = entityPage.stream()
                .map(Deck::getAuthorId)
                .collect(Collectors.toSet());

        log.debug("Fetch usernames for authorIdsCount={}", authorIds.size());
        return userServiceClient.getUsernamesByIds(authorIds);
    }

    /**
     * Selects the most appropriate answer text to display on a flashcard front side.
     * For choice questions, it prioritizes the correct answer text.
     * If no correct answer is found, it falls back to the first non-blank answer text.
     * @param question the question for which to select the answer text
     * @return the selected answer text, or null if no suitable answer is found
     */
    private String selectAnswerText(Question question) {
        if (question.getAnswers() == null || question.getAnswers().isEmpty()) {
            return null;
        }

        return question.getAnswers().stream()
                .filter(ChoiceAnswer.class::isInstance)
                .map(ChoiceAnswer.class::cast)
                .filter(choice -> Boolean.TRUE.equals(choice.getIsCorrect()))
                .map(Answer::getText)
                .findFirst()
                .orElseGet(() -> question.getAnswers().stream()
                        .map(Answer::getText)
                        .filter(text -> text != null && !text.isBlank())
                        .findFirst()
                        .orElse(null));
    }
}
