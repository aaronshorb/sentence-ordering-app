package com.shorb.sentenceordering.repository;

import com.shorb.sentenceordering.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByGradeOrderByUnitNumberAscReadingNumberAsc(int grade);
    List<Exercise> findByGradeAndUnitNumberOrderByReadingNumberAsc(int grade, int unitNumber);
    @Query("select distinct e.unitNumber from Exercise e where e.grade = :grade order by e.unitNumber")
    List<Integer> findDistinctUnitNumbersByGrade(@Param("grade") int grade);
}
