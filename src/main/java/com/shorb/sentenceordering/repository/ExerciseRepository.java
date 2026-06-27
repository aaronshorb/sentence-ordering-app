package com.shorb.sentenceordering.repository;

import com.shorb.sentenceordering.model.Exercise;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByGradeOrderByUnitNumberAscReadingNumberAsc(int grade);
    List<Exercise> findByGradeAndUnitNumberOrderByReadingNumberAsc(int grade, int unitNumber);
    boolean existsByGradeAndUnitNumberAndReadingNumber(int grade, int unitNumber, int readingNumber);
    boolean existsByGradeAndUnitNumberAndReadingNumberAndIdNot(int grade, int unitNumber, int readingNumber, Long id);

    /**
     * Loads an exercise with its sentences for play and edit screens.
     */
    @EntityGraph(attributePaths = "sentences")
    Optional<Exercise> findWithSentencesById(Long id);

    /**
     * Finds unit numbers that have exercises for the selected grade.
     */
    @Query("select distinct e.unitNumber from Exercise e where e.grade = :grade order by e.unitNumber")
    List<Integer> findDistinctUnitNumbersByGrade(@Param("grade") int grade);
}
