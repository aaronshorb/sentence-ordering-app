package com.shorb.sentenceordering.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"grade","unit_number", "reading_number"})
        }
)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(3)
    private int grade;

    @Min(1)
    @Max(20)
    private int unitNumber;

    @Min(1)
    @Max(25)
    private int readingNumber;

    @NotBlank
    private String title;

    private String audioLink;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("correctOrder ASC")
    private List<ExerciseSentence> sentences = new ArrayList<>();

    public Exercise() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(int unitNumber) {
        this.unitNumber = unitNumber;
    }

    public int getReadingNumber() {
        return readingNumber;
    }

    public void setReadingNumber(int readingNumber) {
        this.readingNumber = readingNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }

    public List<ExerciseSentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<ExerciseSentence> sentences) {
        this.sentences = sentences;
    }
}
