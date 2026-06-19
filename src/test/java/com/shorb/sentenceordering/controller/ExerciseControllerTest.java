package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.form.ExerciseForm;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.ExerciseSentence;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.service.ExercisePlayService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ExerciseControllerTest {
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private ExercisePlayService exercisePlayService;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private Model model;

    @InjectMocks
    private ExerciseController controller;

    @Test
    void saveExerciseCreatesSentencesFromReadingTextLines() {
        ExerciseForm exerciseForm = new ExerciseForm();
        exerciseForm.setUnitNumber(2);
        exerciseForm.setReadingNumber(3);
        exerciseForm.setTitle("Title");
        exerciseForm.setReadingText(" First  \n\nSecond ");

        when(bindingResult.hasErrors()).thenReturn(false);

        String viewName = controller.saveExercise(1, exerciseForm, bindingResult, model);

        assertThat(viewName).isEqualTo("redirect:/admin/grades/1/exercises?unitNumber=2");

        verify(exerciseRepository).save(argThat(exercise ->
                        exercise.getSentences().size() == 2 &&
                                exercise.getSentences().get(0).getText().equals("First") &&
                                exercise.getSentences().get(0).getCorrectOrder() == 1 &&
                                exercise.getSentences().get(1).getText().equals("Second") &&
                                exercise.getSentences().get(1).getCorrectOrder() == 2
        ));
    }

    @Test
    void updateExerciseReplacesSentencesFromReadingTextLines() {
        Exercise existingExercise = new Exercise();

        ExerciseSentence oldSentence = new ExerciseSentence();
        oldSentence.setText("Old Sentence");
        oldSentence.setCorrectOrder(1);
        oldSentence.setExercise(existingExercise);
        existingExercise.getSentences().add(oldSentence);

        ExerciseForm exerciseForm = new ExerciseForm();
        exerciseForm.setGrade(1);
        exerciseForm.setUnitNumber(2);
        exerciseForm.setReadingNumber(3);
        exerciseForm.setTitle("Title");
        exerciseForm.setReadingText(" First  \n\nSecond ");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(existingExercise));

        String viewName = controller.updateExercise(1L, exerciseForm, bindingResult, model);

        assertThat(viewName).isEqualTo("redirect:/admin/grades/1/exercises?unitNumber=2");

        verify(exerciseRepository).save(argThat(exercise ->
                exercise == existingExercise &&
                        exercise.getSentences().size() == 2 &&
                        exercise.getSentences().get(0).getText().equals("First") &&
                        exercise.getSentences().get(0).getCorrectOrder() == 1 &&
                        exercise.getSentences().get(1).getText().equals("Second") &&
                        exercise.getSentences().get(1).getCorrectOrder() == 2
        ));
    }

    @Test
    void deleteExerciseRedirectsToGradeExerciseList() {
        Exercise existingExercise = new Exercise();
        existingExercise.setGrade(1);
        existingExercise.setUnitNumber(2);

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(existingExercise));

        String viewName = controller.deleteExercise(1L);

        assertThat(viewName).isEqualTo("redirect:/admin/grades/1/exercises?unitNumber=2");

        verify(exerciseRepository).delete(existingExercise);
    }
}












