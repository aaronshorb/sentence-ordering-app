package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.repository.AppUserRepository;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.service.StudentProgressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class AdminStudentProgressController {

    private final  ExerciseRepository exerciseRepository;
    private final AppUserRepository appUserRepository;
    private final StudentProgressService studentProgressService;

    public AdminStudentProgressController(
            ExerciseRepository exerciseRepository,
            AppUserRepository appUserRepository,
            StudentProgressService studentProgressService
    ) {
        this.exerciseRepository = exerciseRepository;
        this.appUserRepository = appUserRepository;
        this.studentProgressService = studentProgressService;
    }

    @GetMapping("/admin/grades/{grade}/progress")
    public String listUnitsByGrade(@PathVariable int grade, Model model){
        model.addAttribute("grade", grade);
        model.addAttribute("units", exerciseRepository.findDistinctUnitNumbersByGrade(grade));

        return "admin/students/unit-list";
    }

    /**
     * Builds the admin progress table for one grade and unit.
     *
     * @param grade grade whose students are shown
     * @param unitNumber unit whose exercises are shown
     * @param model page model populated with students, exercises, completions, and totals
     * @return admin unit progress view
     */
    @GetMapping("/admin/grades/{grade}/progress/units/{unitNumber}")
    public String showUnitProgress(
            @PathVariable int grade,
            @PathVariable int unitNumber,
            Model model
    ){
        List<Exercise> exercises = exerciseRepository.findByGradeAndUnitNumberOrderByReadingNumberAsc(grade, unitNumber);
        List<AppUser> students = appUserRepository.findByRoleAndGradeOrderByUsernameAsc(AppUser.Role.STUDENT, grade);
        Set<String> completedKeys = studentProgressService.getCompletedExerciseKeys(students, exercises);
        Map<Long, Integer> completedCounts = new HashMap<>();

        for (AppUser student : students) {
            int completedCount = 0;

            for (Exercise exercise : exercises) {
                if (completedKeys.contains(student.getId() + "-" + exercise.getId())) {
                    completedCount++;
                }
            }

            completedCounts.put(student.getId(), completedCount);
        }

        model.addAttribute("grade", grade);
        model.addAttribute("unitNumber", unitNumber);
        model.addAttribute("exercises", exercises);
        model.addAttribute("students", students);
        model.addAttribute("completedKeys", completedKeys);
        model.addAttribute("completedCounts", completedCounts);

        return "admin/students/unit-progress";
    }

    @PostMapping("/admin/grades/{grade}/progress/reset")
    public String resetGradeProgress(@PathVariable int grade){
        studentProgressService.resetGradeCompletions(grade);

        return "redirect:/admin/grades/" + grade + "/exercises";
    }

    @PostMapping("/admin/grades/{grade}/progress/units/{unitNumber}/reset")
    public String resetGradeUnitProgress(
            @PathVariable int grade,
            @PathVariable int unitNumber
    ){
        studentProgressService.resetGradeUnitCompletions(grade, unitNumber);

        return "redirect:/admin/grades/" + grade + "/exercises?unitNumber=" + unitNumber;
    }
}
