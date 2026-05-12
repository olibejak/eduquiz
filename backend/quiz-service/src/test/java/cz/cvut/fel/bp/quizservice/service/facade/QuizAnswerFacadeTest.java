package cz.cvut.fel.bp.quizservice.service.facade;

import cz.cvut.fel.bp.quizservice.client.DeckServiceClient;
import cz.cvut.fel.bp.quizservice.dto.quiz.AnswerSubmitDTO;
import cz.cvut.fel.bp.quizservice.dto.question.QuestionDTO;
import cz.cvut.fel.bp.quizservice.model.QuizParticipant;
import cz.cvut.fel.bp.quizservice.service.QuizParticipantService;
import cz.cvut.fel.bp.quizservice.service.evaluation.AnswerEvaluatorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.choiceAnswer;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.participant;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.question;
import static cz.cvut.fel.bp.quizservice.testutil.TestFixtures.choiceSubmit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAnswerFacadeTest {

    @Mock
    private QuizParticipantFacade participantFacade;

    @Mock
    private DeckServiceClient deckServiceClient;

    @Mock
    private QuizParticipantService participantService;

    @Mock
    private AnswerEvaluatorStrategy firstStrategy;

    @Mock
    private AnswerEvaluatorStrategy secondStrategy;

    private QuizAnswerFacade gameFacade;

    @BeforeEach
    void setUp() {
        gameFacade = new QuizAnswerFacade(participantFacade, participantService, deckServiceClient, List.of(firstStrategy, secondStrategy));
    }

    @Test
    void processAnswerUsesMatchingStrategyAndAwardsPoints() {
        AnswerSubmitDTO answerSubmitDTO = choiceSubmit("PIN123", 1L, 2L, 12L);
        QuestionDTO questionDTO = question(2L, "Capital?", "CHOICE", List.of(
                choiceAnswer(11L, "Berlin", false),
                choiceAnswer(12L, "Paris", true)
        ));
        QuizParticipant participant = participant(1L, cz.cvut.fel.bp.quizservice.testutil.TestFixtures.session("PIN123", UUID.randomUUID(), cz.cvut.fel.bp.quizservice.model.SessionState.QUESTION_ACTIVE), UUID.randomUUID(), "Alice", null, "token-1", 4, true);
        QuizParticipant updatedParticipant = participant(1L, participant.getSession(), participant.getUserId(), participant.getNickname(), participant.getDeviceId(), participant.getToken(), 5, true);

        when(participantFacade.findById(1L, "PIN123")).thenReturn(participant);
        when(deckServiceClient.getQuestionById(2L)).thenReturn(questionDTO);
        when(firstStrategy.supports(answerSubmitDTO)).thenReturn(false);
        when(secondStrategy.supports(answerSubmitDTO)).thenReturn(true);
        when(secondStrategy.evaluate(answerSubmitDTO, questionDTO)).thenReturn(true);
        when(participantFacade.evaluate(1L, 1, true)).thenReturn(updatedParticipant);

        gameFacade.processAnswer(answerSubmitDTO);

        ArgumentCaptor<Integer> pointsCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Boolean> correctCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(participantFacade).evaluate(org.mockito.ArgumentMatchers.eq(1L), pointsCaptor.capture(), correctCaptor.capture());
        assertThat(pointsCaptor.getValue()).isEqualTo(1);
        assertThat(correctCaptor.getValue()).isTrue();
    }

    @Test
    void processAnswerRejectsUnsupportedAnswerType() {
        AnswerSubmitDTO answerSubmitDTO = choiceSubmit("PIN123", 1L, 2L, 12L);
        QuestionDTO questionDTO = question(2L, "Capital?", "CHOICE", List.of(
                choiceAnswer(11L, "Berlin", false),
                choiceAnswer(12L, "Paris", true)
        ));
        QuizParticipant participant = participant(1L, cz.cvut.fel.bp.quizservice.testutil.TestFixtures.session("PIN123", UUID.randomUUID(), cz.cvut.fel.bp.quizservice.model.SessionState.QUESTION_ACTIVE), UUID.randomUUID(), "Alice", null, "token-1", 4, true);

        when(participantFacade.findById(1L, "PIN123")).thenReturn(participant);
        when(deckServiceClient.getQuestionById(2L)).thenReturn(questionDTO);
        when(firstStrategy.supports(answerSubmitDTO)).thenReturn(false);
        when(secondStrategy.supports(answerSubmitDTO)).thenReturn(false);

        assertThatThrownBy(() -> gameFacade.processAnswer(answerSubmitDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid answer type");
        verify(participantFacade).findById(1L, "PIN123");
        verify(deckServiceClient).getQuestionById(2L);
    }
}


