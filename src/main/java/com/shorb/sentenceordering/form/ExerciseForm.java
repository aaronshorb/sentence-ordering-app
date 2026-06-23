package com.shorb.sentenceordering.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ExerciseForm {

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

    @NotBlank
    private String readingText;

    @Pattern(
            regexp = "^$|https?://\\S+$",
            message = "Audio link must be a valid HTTP/HTTPS URL"
    )
    private String audioLink;


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

    public String getReadingText() {
        return readingText;
    }

    public void setReadingText(String readingText) {
        this.readingText = readingText;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }
}
