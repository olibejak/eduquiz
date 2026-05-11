package cz.cvut.fel.bp.deckservice.service.validation;

import cz.cvut.fel.bp.deckservice.model.Answer;
import cz.cvut.fel.bp.deckservice.model.MatchingAnswer;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates answers for MATCHING questions.
 */
@Component
@Slf4j
public class MatchingQuestionValidationStrategy implements QuestionAnswerValidationStrategy {

    @Override
    public QuestionType supports() {
        return QuestionType.MATCHING;
    }

    @Override
    public void validate(Question question) {
        Map<Integer, MatchingGroupState> groupsByMatchId = new HashMap<>();

        for (Answer answer : question.getAnswers()) {
            if (!(answer instanceof MatchingAnswer matchingAnswer)) {
                throw new IllegalArgumentException("MATCHING question can contain only MATCHING answers");
            }

            Integer matchId = matchingAnswer.getMatchId();
            Boolean associate = matchingAnswer.getAssociate();

            if (matchId == null || associate == null) {
                throw new IllegalArgumentException("MATCHING answers must define both matchId and associate");
            }

            MatchingGroupState groupState = groupsByMatchId.computeIfAbsent(matchId, ignored -> new MatchingGroupState());
            if (associate) {
                groupState.hasAssociateTrue = true;
            } else {
                groupState.hasAssociateFalse = true;
            }
        }

        List<Integer> invalidMatchIds = new ArrayList<>();
        for (Map.Entry<Integer, MatchingGroupState> entry : groupsByMatchId.entrySet()) {
            MatchingGroupState groupState = entry.getValue();
            if (!groupState.hasAssociateTrue || !groupState.hasAssociateFalse) {
                invalidMatchIds.add(entry.getKey());
            }
        }

        if (!invalidMatchIds.isEmpty()) {
            throw new IllegalArgumentException("MATCHING question has incomplete pairs for matchIds=" + invalidMatchIds);
        }

        log.debug("Validated MATCHING answers questionId={}, pairsCount={}",
                question.getId(), groupsByMatchId.size());
    }

    private static final class MatchingGroupState {
        private boolean hasAssociateTrue;
        private boolean hasAssociateFalse;
    }
}

