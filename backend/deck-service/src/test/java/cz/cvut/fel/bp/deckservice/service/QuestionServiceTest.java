package cz.cvut.fel.bp.deckservice.service;

import cz.cvut.fel.bp.deckservice.exception.ResourceNotFoundException;
import cz.cvut.fel.bp.deckservice.model.Question;
import cz.cvut.fel.bp.deckservice.model.QuestionType;
import cz.cvut.fel.bp.deckservice.repository.QuestionRepository;
import cz.cvut.fel.bp.deckservice.service.validation.QuestionAnswerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionAnswerValidator questionAnswerValidator;

    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService(questionRepository, questionAnswerValidator);
    }

    @Test
    void shouldValidateQuestionBeforeSave() {
        Question question = Question.builder()
                .text("Q")
                .questionType(QuestionType.WRITE)
                .duration(30)
                .build();

        when(questionRepository.save(question)).thenReturn(question);

        questionService.saveQuestion(question);

        InOrder inOrder = inOrder(questionAnswerValidator, questionRepository);
        inOrder.verify(questionAnswerValidator).validateQuestionAnswers(question);
        inOrder.verify(questionRepository).save(question);
    }

    @Test
    void shouldReturnSavedQuestion() {
        Question question = Question.builder()
                .id(10L)
                .text("Q")
                .questionType(QuestionType.WRITE)
                .duration(30)
                .build();

        when(questionRepository.save(question)).thenReturn(question);

        Question saved = questionService.saveQuestion(question);

        assertThat(saved).isSameAs(question);
    }

    @Test
    void shouldNotSaveQuestionWhenValidationFails() {
        Question question = Question.builder()
                .text("Q")
                .questionType(QuestionType.WRITE)
                .duration(30)
                .build();

        doThrow(new IllegalArgumentException("Invalid answers"))
                .when(questionAnswerValidator)
                .validateQuestionAnswers(question);

        assertThatThrownBy(() -> questionService.saveQuestion(question))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid answers");

        verify(questionRepository, never()).save(question);
    }

    @Test
    void shouldPropagateRepositoryExceptionAfterValidation() {
        Question question = Question.builder()
                .text("Q")
                .questionType(QuestionType.WRITE)
                .duration(30)
                .build();

        when(questionRepository.save(question)).thenThrow(new RuntimeException("DB unavailable"));

        assertThatThrownBy(() -> questionService.saveQuestion(question))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB unavailable");

        InOrder inOrder = inOrder(questionAnswerValidator, questionRepository);
        inOrder.verify(questionAnswerValidator).validateQuestionAnswers(question);
        inOrder.verify(questionRepository).save(question);
    }

    @Test
    void shouldReturnQuestionByIdWhenFound() {
        Question question = Question.builder()
                .id(1L)
                .text("Q")
                .questionType(QuestionType.WRITE)
                .duration(30)
                .build();

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        Question found = questionService.getQuestionById(1L);

        assertThat(found).isSameAs(question);
        verify(questionRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenQuestionByIdIsNotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getQuestionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(questionRepository).findById(99L);
    }
}

