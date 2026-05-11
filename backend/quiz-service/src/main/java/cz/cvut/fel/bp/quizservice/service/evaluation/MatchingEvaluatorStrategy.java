package cz.cvut.fel.bp.quizservice.service.evaluation;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.MatchingAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.question.answerPayload.MatchingAnswerPayload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class MatchingEvaluatorStrategy implements AnswerEvaluatorStrategy {

    @Override
    public boolean supports(AnswerSubmitDTO answerSubmitDTO) {
        return answerSubmitDTO.answerType().equals("MATCHING")
                && answerSubmitDTO.payload() instanceof MatchingAnswerSubmitPayload;
    }

    @Override
    public boolean evaluate(AnswerSubmitDTO answerSubmitDTO, QuestionDTO questionDTO) {
        MatchingAnswerSubmitPayload answerSubmitPayload = (MatchingAnswerSubmitPayload) answerSubmitDTO.payload();
        Map<Long, Long> submittedMatches = answerSubmitPayload.matches();

        if (submittedMatches == null || questionDTO.answers() == null) {
            return false;
        }

        Map<Long, AnswerDTO> answersById = questionDTO.answers().stream()
                .collect(java.util.stream.Collectors.toMap(AnswerDTO::id, answer -> answer));

        return verifySubmittedMatches(submittedMatches, questionDTO)
                && compareMatches(submittedMatches, answersById);
    }

    /**
     * Verifies that the submitted matches contain the correct number of matches
     * and that all source answer IDs in the submitted matches correspond to valid answers in the question.
     * @param submittedMatches a map of submitted matches
     * @param questionDTO the question DTO containing the correct answers
     * @return true if the submitted matches are valid, false otherwise
     */
    private boolean verifySubmittedMatches(Map<Long, Long> submittedMatches, QuestionDTO questionDTO) {
        Set<Long> expectedSourceIds = questionDTO.answers().stream()
                .filter(answer -> answer.payload() instanceof MatchingAnswerPayload(Boolean associate, Integer matchId)
                        && Boolean.TRUE.equals(associate)
                        && matchId != null)
                .map(AnswerDTO::id)
                .collect(java.util.stream.Collectors.toSet());

        return submittedMatches.size() == expectedSourceIds.size()
                && expectedSourceIds.equals(submittedMatches.keySet());
    }

    /**
     * Compares the submitted matches with the correct answers.
     * Checks if each submitted match corresponds to a correct match in the question's answers.
     * @param submittedMatches a map of submitted matches
     * @param answersById a map of answers by their IDs for quick lookup
     * @return true if all submitted matches are correct, false otherwise
     */
    private boolean compareMatches(Map<Long, Long> submittedMatches, Map<Long, AnswerDTO> answersById) {
        return submittedMatches.entrySet().stream().allMatch(entry -> {
            AnswerDTO sourceAnswer = answersById.get(entry.getKey());
            AnswerDTO targetAnswer = answersById.get(entry.getValue());

            if (sourceAnswer == null || targetAnswer == null
                    || !(sourceAnswer.payload() instanceof MatchingAnswerPayload(Boolean associate, Integer matchId))
                    || !(targetAnswer.payload() instanceof MatchingAnswerPayload(Boolean associate1, Integer id))) {
                return false;
            }

            return Boolean.TRUE.equals(associate)
                    && Boolean.FALSE.equals(associate1)
                    && matchId != null
                    && matchId.equals(id);
        });
    }
}
