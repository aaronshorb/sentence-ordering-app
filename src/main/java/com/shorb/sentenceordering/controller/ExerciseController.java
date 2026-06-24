package com.shorb.sentenceordering.controller;

import com.shorb.sentenceordering.exception.ResourceNotFoundException;
import com.shorb.sentenceordering.form.ExerciseForm;
import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.ExerciseSentence;
import com.shorb.sentenceordering.repository.ExerciseRepository;
import com.shorb.sentenceordering.repository.StudentExerciseCompletionRepository;
import com.shorb.sentenceordering.service.ExercisePlayService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ExerciseController {

    private static final String DUPLICATE_EXERCISE_MESSAGE =
            "An exercise for this unit and reading already exists.";

    private final ExerciseRepository exerciseRepository;
    private final StudentExerciseCompletionRepository studentExerciseCompletionRepository;
    private final ExercisePlayService exercisePlayService;

    public ExerciseController(
            ExerciseRepository exerciseRepository,
            StudentExerciseCompletionRepository studentExerciseCompletionRepository,
            ExercisePlayService exercisePlayService
    ) {
        this.exerciseRepository = exerciseRepository;
        this.studentExerciseCompletionRepository = studentExerciseCompletionRepository;
        this.exercisePlayService = exercisePlayService;
    }

    @GetMapping("/admin/grades/{grade}/exercises")
    public String listExercisesByGrade(
            @PathVariable int grade,
            @RequestParam(required = false) Integer unitNumber,
            Model model) {

        model.addAttribute("grade", grade);
        model.addAttribute("units", exerciseRepository.findDistinctUnitNumbersByGrade(grade));
        model.addAttribute("selectedUnit", unitNumber);

        if (unitNumber == null) {
            model.addAttribute("exercises", List.of());
        } else {
            model.addAttribute(
                    "exercises",
                    exerciseRepository.findByGradeAndUnitNumberOrderByReadingNumberAsc(grade, unitNumber)
            );
        }

        return "admin/exercises/list";
    }

    @GetMapping("/admin/grades/{grade}/exercises/new")
    public String showCreateExerciseForm(
            @PathVariable int grade,
            @RequestParam(required = false) Integer unitNumber,
            Model model
    ) {
        ExerciseForm exerciseForm = new ExerciseForm();
        exerciseForm.setGrade(grade);
        exerciseForm.setUnitNumber(unitNumber == null ? 1 : unitNumber);
        exerciseForm.setReadingNumber(1);

        model.addAttribute("exerciseForm", exerciseForm);
        model.addAttribute("grade", grade);
        model.addAttribute("backUrl", createGradeExerciseListUrl(grade, unitNumber));

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

        if (duplicateExerciseExists(exerciseForm)) {
            rejectDuplicateExercise(bindingResult);
            model.addAttribute("grade", grade);
            return "admin/exercises/form";
        }

        Exercise exercise = new Exercise();
        updateExerciseFromForm(exerciseForm, exercise);

        exerciseRepository.save(exercise);
        return "redirect:/admin/grades/" + grade + "/exercises?unitNumber=" + exercise.getUnitNumber();
    }

    @GetMapping("/admin/exercises/{id}/play")
    public String previewExercise(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseRepository.findWithSentencesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
        List<ExerciseSentence> shuffledSentences = exercisePlayService.shuffledSentences(exercise);

        model.addAttribute("exercise", exercise);
        model.addAttribute("sentenceFeedback", exercisePlayService.uncheckedSentenceFeedback(shuffledSentences));
        model.addAttribute("backUrl", "/admin/grades/" + exercise.getGrade() + "/exercises?unitNumber=" + exercise.getUnitNumber());
        model.addAttribute("playAction", "/admin/exercises/" + id + "/play");

        return "exercises/play";
    }

    @PostMapping("/admin/exercises/{id}/play")
    public String checkExercisePreview(
            @PathVariable Long id,
            @RequestParam List<Long> sentenceIds,
            Model model
    ) {
        Exercise exercise = exerciseRepository.findWithSentencesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
        ExercisePlayService.ExerciseAnswerResult result =
                exercisePlayService.checkAnswer(exercise, sentenceIds);

        model.addAttribute("exercise", exercise);
        model.addAttribute("sentenceFeedback", result.displayedSentences());
        model.addAttribute("resultMessage", result.correct()
                ? "Right! All sentences are in the correct order."
                : "Some sentences are out of order. Rearrange the highlighted sentences.");
        model.addAttribute("resultCorrect", result.correct());
        model.addAttribute("backUrl", "/admin/grades/" + exercise.getGrade() + "/exercises?unitNumber=" + exercise.getUnitNumber());
        model.addAttribute("playAction", "/admin/exercises/" + id + "/play");

        return "exercises/play";
    }

    @GetMapping("/admin/exercises/{id}/edit")
    public String showEditExerciseForm(@PathVariable Long id, Model model) {
        Exercise exercise = exerciseRepository.findWithSentencesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));

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

        if (duplicateExerciseExists(exerciseForm)) {
            rejectDuplicateExercise(bindingResult);
            model.addAttribute("grade", exerciseForm.getGrade());
            return "admin/exercises/edit";
        }

        Exercise exercise = exerciseRepository.findWithSentencesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
        updateExerciseFromForm(exerciseForm, exercise);
        exerciseRepository.save(exercise);
        return "redirect:/admin/grades/" + exercise.getGrade() + "/exercises?unitNumber=" + exercise.getUnitNumber();
    }

    @PostMapping("/admin/exercises/{id}/delete")
    @Transactional
    public String deleteExercise(@PathVariable("id") long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
        int grade = exercise.getGrade();
        int unitNumber = exercise.getUnitNumber();

        studentExerciseCompletionRepository.deleteByExercise(exercise);
        exerciseRepository.delete(exercise);
        return "redirect:/admin/grades/" + grade + "/exercises?unitNumber=" + unitNumber;
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

    private boolean duplicateExerciseExists(ExerciseForm form) {
        if (form.getId() == null) {
            return exerciseRepository.existsByGradeAndUnitNumberAndReadingNumber(
                    form.getGrade(),
                    form.getUnitNumber(),
                    form.getReadingNumber()
            );
        }

        return exerciseRepository.existsByGradeAndUnitNumberAndReadingNumberAndIdNot(
                form.getGrade(),
                form.getUnitNumber(),
                form.getReadingNumber(),
                form.getId()
        );
    }

    private void rejectDuplicateExercise(BindingResult bindingResult) {
        bindingResult.reject("duplicateExercise", DUPLICATE_EXERCISE_MESSAGE);
    }

    private String createGradeExerciseListUrl(int grade, Integer unitNumber) {
        String url = "/admin/grades/" + grade + "/exercises";

        if (unitNumber != null) {
            return url + "?unitNumber=" + unitNumber;
        }

        return url;
    }

}
