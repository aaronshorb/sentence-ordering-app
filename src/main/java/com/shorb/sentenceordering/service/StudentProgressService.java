package com.shorb.sentenceordering.service;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.StudentExerciseCompletion;
import com.shorb.sentenceordering.repository.StudentExerciseCompletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentProgressService {

    private final StudentExerciseCompletionRepository studentExerciseCompletionRepository;

    public StudentProgressService(StudentExerciseCompletionRepository studentExerciseCompletionRepository) {
        this.studentExerciseCompletionRepository = studentExerciseCompletionRepository;
    }

    @Transactional
    public void resetGradeCompletions(int grade) {
        studentExerciseCompletionRepository.deleteByStudentGrade(grade);
    }

    @Transactional
    public void resetGradeUnitCompletions(int grade, int unitNumber) {
        studentExerciseCompletionRepository.deleteByStudentGradeAndExerciseUnitNumber(grade, unitNumber);
    }

    @Transactional
    public void resetStudentCompletions(AppUser student, int unitNumber) {
        studentExerciseCompletionRepository.deleteByStudentAndExerciseUnitNumber(student, unitNumber);
    }

    /**
     * Saves a completion record when a student answers correctly.
     *
     * @param student student who submitted the exercise
     * @param exercise exercise that was submitted
     * @param correct whether the answer was correct
     */
    @Transactional
    public void markCompletedIfCorrect(AppUser student, Exercise exercise, boolean correct) {
        if (correct && !studentExerciseCompletionRepository.existsByStudentAndExercise(student, exercise)) {

            StudentExerciseCompletion completion = new StudentExerciseCompletion();
            completion.setStudent(student);
            completion.setExercise(exercise);

            studentExerciseCompletionRepository.save(completion);
        }
    }

    /**
     * Gets completed exercise IDs for showing a student's completed readings.
     *
     * @param student student whose completions should be loaded
     * @return IDs of exercises the student has completed
     */
    public List<Long> getCompletedExerciseIdsByStudent(AppUser student) {
        return studentExerciseCompletionRepository.findByStudent(student)
                .stream()
                .map(completion -> completion.getExercise().getId())
                .toList();
    }


    /**
     * Builds the lookup keys for the admin's student progress table.
     *
     * @param students students shown in the progress table
     * @param exercises exercises shown in the progress table
     * @return keys in "studentId-exerciseId" format for completed exercises
     */
    public Set<String> getCompletedExerciseKeys(List<AppUser> students, List<Exercise> exercises) {
        return studentExerciseCompletionRepository.findByStudentInAndExerciseIn(students, exercises)
                .stream()
                .map(completion ->
                        completion.getStudent().getId() + "-" + completion.getExercise().getId())
                .collect(Collectors.toSet());
    }
}
