package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.exception.ResourceNotFoundException;
import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.ExerciseSentence;
import com.shorb.sentenceordering.repository.AppUserRepository;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.service.ExercisePlayService;
import com.shorb.sentenceordering.service.StudentProgressService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StudentExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final AppUserRepository appUserRepository;
    private final ExercisePlayService exercisePlayService;
    private final StudentProgressService studentProgressService;

    public StudentExerciseController(
            ExerciseRepository exerciseRepository,
            AppUserRepository appUserRepository,
            ExercisePlayService exercisePlayService,
            StudentProgressService studentProgressService
    ) {
        this.exerciseRepository = exerciseRepository;
        this.appUserRepository = appUserRepository;
        this.exercisePlayService = exercisePlayService;
        this.studentProgressService = studentProgressService;
    }

    @GetMapping("/student/exercises")
    public String listUnits(Authentication authentication, Model model) {
        AppUser student = getCurrentUser(authentication);

        List<Integer> units = exerciseRepository.findDistinctUnitNumbersByGrade(student.getGrade());
        Map<Integer, Long> readingCounts = exerciseRepository.findByGradeOrderByUnitNumberAscReadingNumberAsc(student.getGrade())
                .stream()
                .collect(Collectors.groupingBy(
                        Exercise::getUnitNumber,
                        Collectors.counting()
                ));

        model.addAttribute("units", units);
        model.addAttribute("readingCounts", readingCounts);

        return "student/units";
    }

    @GetMapping("/student/exercises/units/{unitNumber}")
    public String listExercisesByUnit(
            @PathVariable int unitNumber,
            Authentication authentication,
            Model model
    ) {
        AppUser student = getCurrentUser(authentication);

        List<Exercise> exercises = exerciseRepository.findByGradeAndUnitNumberOrderByReadingNumberAsc(student.getGrade(), unitNumber);
        List<Long> completedExerciseIds = studentProgressService.getCompletedExerciseIdsByStudent(student);

        model.addAttribute("unitNumber", unitNumber);
        model.addAttribute("exercises", exercises);
        model.addAttribute("completedExerciseIds", completedExerciseIds);

        return "student/unit-readings";
    }

    @GetMapping("/student/exercises/{id}/play")
    public String playExercise(@PathVariable Long id, Authentication authentication, Model model) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
        AppUser student = getCurrentUser(authentication);

        checkExerciseAccess(student, exercise);

        List<ExerciseSentence> shuffledSentences = exercisePlayService.shuffledSentences(exercise);

        model.addAttribute("exercise", exercise);
        model.addAttribute("sentenceFeedback", exercisePlayService.uncheckedSentenceFeedback(shuffledSentences));
        model.addAttribute("backUrl", "/student/exercises/units/" + exercise.getUnitNumber());
        model.addAttribute("playAction", "/student/exercises/" + id + "/play");

        return "exercises/play";
    }

    @PostMapping("/student/exercises/{id}/play")
    public String checkExercise(
            @PathVariable Long id,
            @RequestParam List<Long> sentenceIds,
            Authentication authentication,
            Model model
    ){
        AppUser student = getCurrentUser(authentication);

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));

        checkExerciseAccess(student, exercise);

        ExercisePlayService.ExerciseAnswerResult result =
                exercisePlayService.checkAnswer(exercise, sentenceIds);

        studentProgressService.markCompletedIfCorrect(student, exercise, result.correct());

        model.addAttribute("exercise", exercise);
        model.addAttribute("sentenceFeedback", result.displayedSentences());
        model.addAttribute("resultMessage", result.correct()
                ? "Right! All sentences are in the correct order."
                : "Some sentences are out of order. Rearrange the highlighted sentences.");
        model.addAttribute("resultCorrect", result.correct());
        model.addAttribute("backUrl", "/student/exercises/units/" + exercise.getUnitNumber());
        model.addAttribute("playAction", "/student/exercises/" + id + "/play");

        return "exercises/play";
    }

    @PostMapping("/student/exercises/units/{unitNumber}/progress/reset")
    public String resetStudentProgress(@PathVariable int unitNumber, Authentication authentication) {
        AppUser student = getCurrentUser(authentication);

        studentProgressService.resetStudentCompletions(student, unitNumber);

        return "redirect:/student/exercises/units/" + unitNumber;
    }

    private AppUser getCurrentUser(Authentication authentication) {
        return appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: "+ authentication.getName()));
    }

    private void checkExerciseAccess(AppUser user, Exercise exercise) {
        if (user.getRole() == AppUser.Role.ADMIN) {
            return;
        }
        if (user.getGrade() == null || exercise.getGrade() != user.getGrade()) {
            throw new AccessDeniedException("You cannot access this exercise.");
        }
    }
}
