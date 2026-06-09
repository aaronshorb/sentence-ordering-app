package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.form.ExerciseForm;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.ExerciseSentence;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.service.ExercisePlayService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ExerciseController {

    private final ExerciseRepository exerciseRepository;
    private final ExercisePlayService exercisePlayService;

    public ExerciseController(
            ExerciseRepository exerciseRepository,
            ExercisePlayService exercisePlayService
    ) {
        this.exerciseRepository = exerciseRepository;
        this.exercisePlayService = exercisePlayService;
    }

    @GetMapping("/admin/grades/{grade}/exercises")
    public String listExercisesByGrade(@PathVariable int grade, Model model) {
        model.addAttribute(
                "exercises",
                exerciseRepository.findByGradeOrderByUnitNumberAscReadingNumberAsc(grade)
        );

        model.addAttribute("grade", grade);

        return "admin/exercises/list";
    }

    @GetMapping("/admin/grades/{grade}/exercises/new")
    public String showCreateExerciseForm(@PathVariable int grade, Model model) {
        ExerciseForm exerciseForm = new ExerciseForm();
        exerciseForm.setGrade(grade);
        exerciseForm.setUnitNumber(1);
        exerciseForm.setReadingNumber(1);

        model.addAttribute("exerciseForm", exerciseForm);
        model.addAttribute("grade", grade);

        return "admin/exercises/form";
    }

    @PostMapping("/admin/grades/{grade}/exercises")
    public String saveExercise(
            @PathVariable int grade,
            @Valid ExerciseForm exerciseForm,
            BindingResult bindingResult,
            Model model)
    {
        exerciseForm.setGrade(grade);

        if (bindingResult.hasErrors()){
            model.addAttribute("grade", grade);
            return "admin/exercises/form";
        }

        Exercise exercise = new Exercise();
        updateExerciseFromForm(exerciseForm, exercise);

        exerciseRepository.save(exercise);
        return "redirect:/admin/grades/" + grade + "/exercises";
    }

    @GetMapping("/admin/exercises/{id}/play")
    public String previewExercise(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise id " + id + " not found."));

        model.addAttribute("exercise", exercise);
        model.addAttribute("shuffledSentences", exercisePlayService.shuffledSentences(exercise));
        model.addAttribute("backUrl", "/admin/grades/" + exercise.getGrade() + "/exercises");
        model.addAttribute("playAction", "/admin/exercises/" + id + "/play");

        return "exercises/play";
    }

    @PostMapping("/admin/exercises/{id}/play")
    public String checkExercisePreview(
            @PathVariable Long id,
            @RequestParam List<Long> sentenceIds,
            @RequestParam List<Integer> orders,
            Model model
    ) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid exercise id " + id + " not found."));

        ExercisePlayService.ExerciseAnswerResult result =
                exercisePlayService.checkAnswer(exercise, sentenceIds, orders);

        model.addAttribute("exercise", exercise);
        model.addAttribute("shuffledSentences", result.displayedSentences());
        model.addAttribute("displayedOrders", result.displayedOrders());
        model.addAttribute("resultMessage", result.correct() ? "Right!" : "Wrong. Try again.");
        model.addAttribute("backUrl", "/admin/grades/" + exercise.getGrade() + "/exercises");
        model.addAttribute("playAction", "/admin/exercises/" + id + "/play");

        return "exercises/play";
    }

    @GetMapping("/admin/exercises/{id}/edit")
    public String showEditExerciseForm(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Exercise id " + id + " not found."));
        ExerciseForm exerciseForm = createFormFromExercise(exercise);

        model.addAttribute("exerciseForm", exerciseForm);
        model.addAttribute("grade", exercise.getGrade());

        return "admin/exercises/edit";
    }

    @PostMapping("/admin/exercises/{id}/edit")
    public String updateExercise(
            @PathVariable Long id,
            @Valid ExerciseForm exerciseForm,
            BindingResult bindingResult,
            Model model
    ) {
        exerciseForm.setId(id);

        if (bindingResult.hasErrors()){
            model.addAttribute("grade", exerciseForm.getGrade());
            return "admin/exercises/edit";
        }

        Exercise exercise = exerciseRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Exercise id " + id + " not found."));

        updateExerciseFromForm(exerciseForm, exercise);
        exerciseRepository.save(exercise);
        return "redirect:/admin/grades/" + exercise.getGrade() + "/exercises";
    }

    @PostMapping("/admin/exercises/{id}/delete")
    public String deleteExercise(@PathVariable("id") long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Exercise id " + id + " not found."));

        int grade = exercise.getGrade();

        exerciseRepository.delete(exercise);
        return "redirect:/admin/grades/" + grade + "/exercises";
    }

    private void updateExerciseFromForm(ExerciseForm form, Exercise exercise) {
        exercise.setGrade(form.getGrade());
        exercise.setUnitNumber(form.getUnitNumber());
        exercise.setReadingNumber(form.getReadingNumber());
        exercise.setTitle(form.getTitle());
        exercise.setAudioLink(form.getAudioLink());

        exercise.getSentences().clear();

        String[] lines = form.getReadingText().split("\\R");
        int order = 1;

        for (String l : lines) {
            String text = l.trim();

            if (!text.isEmpty()) {
                ExerciseSentence sentence = new ExerciseSentence();
                sentence.setText(text);
                sentence.setCorrectOrder(order);
                sentence.setExercise(exercise);

                exercise.getSentences().add(sentence);
                order++;
            }
        }
    }

    private ExerciseForm createFormFromExercise(Exercise exercise) {
        ExerciseForm form = new ExerciseForm();

        form.setId(exercise.getId());
        form.setGrade(exercise.getGrade());
        form.setUnitNumber(exercise.getUnitNumber());
        form.setReadingNumber(exercise.getReadingNumber());
        form.setTitle(exercise.getTitle());
        form.setAudioLink(exercise.getAudioLink());

        StringBuilder readingText = new StringBuilder();

        for (ExerciseSentence s : exercise.getSentences()) {
            if (!readingText.isEmpty()) {
                readingText.append(System.lineSeparator());
            }

            readingText.append(s.getText());
        }

        form.setReadingText(readingText.toString());

        return form;
    }

}
