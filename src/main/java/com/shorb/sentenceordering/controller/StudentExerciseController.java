package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.model.AppUser;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.StudentExerciseCompletion;
import com.shorb.sentenceordering.repository.AppUserRepository;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.repository.StudentExerciseCompletionRepository;
import com.shorb.sentenceordering.service.ExercisePlayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Controller
public class StudentExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final AppUserRepository appUserRepository;
    private final ExercisePlayService exercisePlayService;
    private final StudentExerciseCompletionRepository completionRepository;

    public StudentExerciseController(
            ExerciseRepository exerciseRepository,
            AppUserRepository appUserRepository,
            ExercisePlayService exercisePlayService,
            StudentExerciseCompletionRepository completionRepository
    ) {
        this.exerciseRepository = exerciseRepository;
        this.appUserRepository = appUserRepository;
        this.exercisePlayService = exercisePlayService;
        this.completionRepository = completionRepository;
    }

    @GetMapping("/student/exercises")
    public String listExercises(Authentication authentication, Model model) {
        AppUser user = getCurrentUser(authentication);

        List<Exercise> exercises = exerciseRepository.findByGradeOrderByUnitNumberAscReadingNumberAsc(user.getGrade());
        List<Long> completedExerciseIds = completionRepository.findByStudent(user)
                .stream()
                .map(completion -> completion.getExercise().getId())
                .toList();

        model.addAttribute("exercises", exercises);
        model.addAttribute("completedExerciseIds", completedExerciseIds);

        return "student/exercise-list";
    }

    @GetMapping("/student/exercises/{id}/play")
    public String playExercise(@PathVariable Long id, Authentication authentication, Model model) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Exercise id " + id + " not found."));

        AppUser user = getCurrentUser(authentication);

        checkExerciseAccess(user, exercise);

        model.addAttribute("exercise", exercise);
        model.addAttribute("shuffledSentences", exercisePlayService.shuffledSentences(exercise));
        model.addAttribute("backUrl", "/student/exercises");
        model.addAttribute("playAction", "/student/exercises/" + id + "/play");

        return "exercises/play";
    }

    @PostMapping("/student/exercises/{id}/play")
    public String checkExercise(
            @PathVariable Long id,
            @RequestParam List<Long> sentenceIds,
            @RequestParam List<Integer> orders,
            Authentication authentication,
            Model model
    ){
        AppUser user = getCurrentUser(authentication);

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Exercise id " + id + " not found."));

        checkExerciseAccess(user, exercise);

        ExercisePlayService.ExerciseAnswerResult result =
                exercisePlayService.checkAnswer(exercise, sentenceIds, orders);

        if (result.correct()
                && !completionRepository.existsByStudentAndExercise(user, exercise)) {
            StudentExerciseCompletion completion = new StudentExerciseCompletion();
            completion.setStudent(user);
            completion.setExercise(exercise);
            completionRepository.save(completion);
        }

        model.addAttribute("exercise", exercise);
        model.addAttribute("shuffledSentences", result.displayedSentences());
        model.addAttribute("displayedOrders", result.displayedOrders());
        model.addAttribute("resultMessage", result.correct() ? "Right!" : "Wrong. Try again.");
        model.addAttribute("backUrl", "/student/exercises");
        model.addAttribute("playAction", "/student/exercises/" + id + "/play");

        return "exercises/play";
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
