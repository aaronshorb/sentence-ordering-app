package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.repository.AppUserRepository;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.service.ExercisePlayService;
import com.shorb.sentenceordering.service.StudentProgressService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class StudentExerciseControllerTest {
    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private ExercisePlayService exercisePlayService;
    @Mock
    private StudentProgressService studentProgressService;
    @Mock
    private Authentication authentication;
    @Mock
    private Model model;

    @InjectMocks
    private StudentExerciseController controller;

    @Test
    void studentCanAccessExerciseFromSameGrade() {
        AppUser student = new AppUser();
        student.setRole(AppUser.Role.STUDENT);
        student.setGrade(1);

        Exercise exercise = new Exercise();
        exercise.setGrade(1);
        exercise.setUnitNumber(2);

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(student));
        when(exerciseRepository.findWithSentencesById(1L)).thenReturn(Optional.of(exercise));
        when(exercisePlayService.shuffledSentences(exercise)).thenReturn(List.of());

        String viewName = controller.playExercise(1L, authentication, model);

        assertThat(viewName).isEqualTo("exercises/play");
    }

    @Test
    void studentCannotAccessExerciseFromDifferentGrade() {
        AppUser student = new AppUser();
        student.setRole(AppUser.Role.STUDENT);
        student.setGrade(1);

        Exercise exercise = new Exercise();
        exercise.setGrade(2);
        exercise.setUnitNumber(2);

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(student));
        when(exerciseRepository.findWithSentencesById(1L)).thenReturn(Optional.of(exercise));

        assertThatThrownBy(() -> controller.playExercise(1L, authentication, model))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You cannot access this exercise.");

        verify(exercisePlayService, never()).shuffledSentences(exercise);
    }

    @Test
    void studentWithNullGradeCannotAccessExercise() {
        AppUser student = new AppUser();
        student.setRole(AppUser.Role.STUDENT);
        student.setGrade(null);

        Exercise exercise = new Exercise();
        exercise.setGrade(1);
        exercise.setUnitNumber(2);

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(student));
        when(exerciseRepository.findWithSentencesById(1L)).thenReturn(Optional.of(exercise));

        assertThatThrownBy(() -> controller.playExercise(1L, authentication, model))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You cannot access this exercise.");

        verify(exercisePlayService, never()).shuffledSentences(exercise);
    }

    @Test
    void adminCanAccessExercise() {
        AppUser admin = new AppUser();
        admin.setRole(AppUser.Role.ADMIN);
        admin.setGrade(null);

        Exercise exercise = new Exercise();
        exercise.setGrade(1);
        exercise.setUnitNumber(2);

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(admin));
        when(exerciseRepository.findWithSentencesById(1L)).thenReturn(Optional.of(exercise));
        when(exercisePlayService.shuffledSentences(exercise)).thenReturn(List.of());

        String viewName = controller.playExercise(1L, authentication, model);

        assertThat(viewName).isEqualTo("exercises/play");
    }

    @Test
    void exercisePassesTrueWhenCorrect() {
        AppUser student = new AppUser();
        student.setRole(AppUser.Role.STUDENT);
        student.setGrade(1);

        Exercise exercise = new Exercise();
        exercise.setGrade(1);
        exercise.setUnitNumber(2);

        List<Long> sentenceIds = List.of(1L, 2L);

        ExercisePlayService.ExerciseAnswerResult result =
                new ExercisePlayService.ExerciseAnswerResult(
                        true,
                        List.of()
                );

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(student));
        when(exerciseRepository.findWithSentencesById(1L)).thenReturn(Optional.of(exercise));
        when(exercisePlayService.checkAnswer(exercise, sentenceIds)).thenReturn(result);

        controller.checkExercise(
                1L,
                sentenceIds,
                authentication,
                model
        );

        verify(studentProgressService).markCompletedIfCorrect(student, exercise, true);
    }

    @Test
    void exercisePassesFalseWhenIncorrect() {
        AppUser student = new AppUser();
        student.setRole(AppUser.Role.STUDENT);
        student.setGrade(1);

        Exercise exercise = new Exercise();
        exercise.setGrade(1);
        exercise.setUnitNumber(2);

        List<Long> sentenceIds = List.of(1L, 2L);

        ExercisePlayService.ExerciseAnswerResult result =
                new ExercisePlayService.ExerciseAnswerResult(
                        false,
                        List.of()
                );

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(student));
        when(exerciseRepository.findWithSentencesById(1L)).thenReturn(Optional.of(exercise));
        when(exercisePlayService.checkAnswer(exercise, sentenceIds)).thenReturn(result);

        controller.checkExercise(
                1L,
                sentenceIds,
                authentication,
                model
        );

        verify(studentProgressService).markCompletedIfCorrect(student, exercise, false);
    }

    @Test
    void resetStudentCompletionsResetsProgressAndRedirects() {
        AppUser student = new AppUser();

        when(authentication.getName()).thenReturn("aaron");
        when(appUserRepository.findByUsername("aaron")).thenReturn(Optional.of(student));

        String viewName = controller.resetStudentProgress(2, authentication);

        assertThat(viewName).isEqualTo("redirect:/student/exercises/units/2");

        verify(studentProgressService).resetStudentCompletions(student, 2);
    }
}
