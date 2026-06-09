package com.shorb.sentenceordering.repository;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.StudentExerciseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentExerciseCompletionRepository extends JpaRepository<StudentExerciseCompletion, Long> {
    boolean existsByStudentAndExercise(AppUser student, Exercise exercise);
    List<StudentExerciseCompletion> findByStudent(AppUser student);
    List<StudentExerciseCompletion> findByStudentInAndExerciseIn(List<AppUser> students, List<Exercise> exercises);
}
