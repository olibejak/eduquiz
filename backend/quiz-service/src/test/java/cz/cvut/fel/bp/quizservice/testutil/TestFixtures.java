package cz.cvut.fel.bp.quizservice.testutil;

import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.ChoiceAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.MatchingAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.quiz.answerPayload.StandardAnswerSubmitPayload;
import cz.cvut.fel.bp.quizservice.dto.join.JoinRequestDTO;
import cz.cvut.fel.bp.quizservice.dto.question.AnswerDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import cz.cvut.fel.bp.quizservice.dto.question.answerPayload.ChoiceAnswerPayload;
import cz.cvut.fel.bp.quizservice.dto.question.answerPayload.MatchingAnswerPayload;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.model.QuizSession;
import cz.cvut.fel.bp.quizservice.model.SessionState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class TestFixtures {

    private TestFixtures() {
    }

    public static QuizSession session(String pin, UUID hostUserId, SessionState state) {
        return QuizSession.builder()
                .lobbyPin(pin)
                .state(state)
                .build();
    }

    public static QuizParticipant participant(Long id, QuizSession session, UUID userId, String nickname, String deviceId, String token, int currentScore, boolean connected) {
        QuizParticipant participant = QuizParticipant.builder()
                .userId(userId)
                .nickname(nickname)
                .deviceId(deviceId)
                .token(token)
                .currentScore(currentScore)
                .isConnected(connected)
                .build();
        participant.setId(id);
        participant.setSession(session);
        return participant;
    }

    public static QuestionDTO question(Long id, String text, String type, List<AnswerDTO> answers) {
        return new QuestionDTO(id, text, type, answers, 30);
    }

    public static AnswerDTO answer(Long id, String text, String type, Object payload) {
        return AnswerDTO.builder()
                .id(id)
                .text(text)
                .type(type)
                .payload((cz.cvut.fel.bp.quizservice.dto.question.answerPayload.AnswerPayload) payload)
                .build();
    }

    public static AnswerDTO choiceAnswer(Long id, String text, boolean correct) {
        return answer(id, text, "CHOICE", new ChoiceAnswerPayload(correct));
    }

    public static AnswerDTO matchingAnswer(Long id, String text, boolean associate, int matchId) {
        return answer(id, text, "MATCHING", new MatchingAnswerPayload(associate, matchId));
    }

    public static QuestionDTO standardQuestion(String correctText) {
        return question(1L, "question", "STANDARD", List.of(
                answer(10L, correctText, "STANDARD", null)
        ));
    }

    public static AnswerSubmitDTO standardSubmit(String lobbyPin, Long participantId, Long questionId, String text) {
        return new AnswerSubmitDTO(lobbyPin, participantId, questionId, "STANDARD", new StandardAnswerSubmitPayload("STANDARD", text));
    }

    public static AnswerSubmitDTO choiceSubmit(String lobbyPin, Long participantId, Long questionId, Long answerId) {
        return new AnswerSubmitDTO(lobbyPin, participantId, questionId, "CHOICE", new ChoiceAnswerSubmitPayload("CHOICE", answerId));
    }

    public static AnswerSubmitDTO matchingSubmit(String lobbyPin, Long participantId, Long questionId, Map<Long, Long> matches) {
        return new AnswerSubmitDTO(lobbyPin, participantId, questionId, "MATCHING", new MatchingAnswerSubmitPayload("MATCHING", matches));
    }

    public static JoinRequestDTO joinRequest(UUID userId, String nickname, String deviceId) {
        return new JoinRequestDTO(userId, nickname, deviceId);
    }
}


