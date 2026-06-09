package com.shorb.sentenceordering.service;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.repository.StudentExerciseCompletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void resetStudentCompletions(AppUser student, int unitNumber) {
        studentExerciseCompletionRepository.deleteByStudentAndExerciseUnitNumber(student, unitNumber);
    }

}
