package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.repository.AppUserRepository;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.repository.StudentExerciseCompletionRepository;
import com.shorb.sentenceordering.service.StudentProgressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminStudentProgressController {

    private final  ExerciseRepository exerciseRepository;
    private final AppUserRepository appUserRepository;
    private final StudentExerciseCompletionRepository studentExerciseCompletionRepository;
    private final StudentProgressService studentProgressService;

    public AdminStudentProgressController(
            ExerciseRepository exerciseRepository,
            AppUserRepository appUserRepository,
            StudentExerciseCompletionRepository studentExerciseCompletionRepository,
            StudentProgressService studentProgressService
    ) {
        this.exerciseRepository = exerciseRepository;
        this.appUserRepository = appUserRepository;
        this.studentExerciseCompletionRepository = studentExerciseCompletionRepository;
        this.studentProgressService = studentProgressService;
    }

    @GetMapping("/admin/grades/{grade}/progress")
    public String listUnitsByGrade(@PathVariable int grade, Model model){
        model.addAttribute("grade", grade);
        model.addAttribute("units", exerciseRepository.findDistinctUnitNumbersByGrade(grade));

        return "admin/students/unit-list";
    }

    @GetMapping("/admin/grades/{grade}/progress/units/{unitNumber}")
    public String showUnitProgress(
            @PathVariable int grade,
            @PathVariable int unitNumber,
            Model model
    ){
        List<Exercise> exercises = exerciseRepository.findByGradeAndUnitNumberOrderByReadingNumberAsc(grade, unitNumber);
        List<AppUser> students = appUserRepository.findByRoleAndGradeOrderByUsernameAsc(AppUser.Role.STUDENT, grade);
        List<String> completedKeys = studentExerciseCompletionRepository.findByStudentInAndExerciseIn(students, exercises)
                .stream()
                .map(completion ->
                        completion.getStudent().getId() + "-" + completion.getExercise().getId())
                .toList();


        model.addAttribute("grade", grade);
        model.addAttribute("unitNumber", unitNumber);
        model.addAttribute( "exercises", exercises);
        model.addAttribute("students", students);
        model.addAttribute("completedKeys", completedKeys);

        return "admin/students/unit-progress";
    }

    @PostMapping("/admin/grades/{grade}/progress/reset")
    public String resetGradeProgress(@PathVariable int grade){
        studentProgressService.resetGradeCompletions(grade);

        return "redirect:/admin/grades/" + grade + "/exercises";
    }
}
