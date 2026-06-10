package com.shorb.sentenceordering.service;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.repository.StudentExerciseCompletionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class StudentProgressServiceTest {

    @Mock
    private StudentExerciseCompletionRepository completionRepository;

    @InjectMocks
    private StudentProgressService service;

    @Test
    void marksCompletedWhenCorrectAndNotAlreadyCompleted() {
        AppUser student = new AppUser();
        Exercise exercise = new Exercise();

        when(completionRepository.existsByStudentAndExercise(student, exercise)).thenReturn(false);

        service.markCompletedIfCorrect(student, exercise, true);

        verify(completionRepository).save(argThat(completion ->
                completion.getStudent() == student && completion.getExercise() == exercise
        ));
    }

    @Test
    void doesNotSaveWhenAnswerIsWrong() {
        AppUser student = new AppUser();
        Exercise exercise = new Exercise();

        service.markCompletedIfCorrect(student, exercise, false);

        verify(completionRepository, never()).save(any());
    }

    @Test
    void doesNotSaveWhenAlreadyCompleted() {
        AppUser student = new AppUser();
        Exercise exercise = new Exercise();

        when(completionRepository.existsByStudentAndExercise(student, exercise)).thenReturn(true);

        service.markCompletedIfCorrect(student, exercise, true);

        verify(completionRepository, never()).save(any());
    }
}
