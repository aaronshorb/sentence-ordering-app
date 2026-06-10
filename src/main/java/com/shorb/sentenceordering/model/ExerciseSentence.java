package com.shorb.sentenceordering.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class ExerciseSentence{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String text;

    private int correctOrder;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;


    public ExerciseSentence() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCorrectOrder() {
        return correctOrder;
    }

    public void setCorrectOrder(int correctOrder) {
        this.correctOrder = correctOrder;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
}