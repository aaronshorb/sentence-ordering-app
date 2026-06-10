package com.shorb.sentenceordering.service;

import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.ExerciseSentence;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ExercisePlayServiceTest {

    private final ExercisePlayService service = new ExercisePlayService();

    @Test
    void correctAnswerReturnsCorrectResult() {
        Exercise exercise = new Exercise();

        ExerciseSentence first = new ExerciseSentence();
        first.setId(1L);
        first.setText("First");
        first.setCorrectOrder(1);
        first.setExercise(exercise);

        ExerciseSentence second = new ExerciseSentence();
        second.setId(2L);
        second.setText("Second");
        second.setCorrectOrder(2);
        second.setExercise(exercise);

        exercise.getSentences().add(first);
        exercise.getSentences().add(second);

        var result = service.checkAnswer(
                exercise,
                List.of(1L, 2L),
                List.of(1, 2)
        );

        assertThat(result.correct()).isTrue();
        assertThat(result.displayedSentences()).containsExactly(first, second);
        assertThat(result.displayedOrders()).isEmpty();
    }

    @Test
    void wrongAnswerReturnsWrongResult() {
        Exercise exercise = new Exercise();

        ExerciseSentence first = new ExerciseSentence();
        first.setId(1L);
        first.setText("First");
        first.setCorrectOrder(1);
        first.setExercise(exercise);

        ExerciseSentence second = new ExerciseSentence();
        second.setId(2L);
        second.setText("Second");
        second.setCorrectOrder(2);
        second.setExercise(exercise);

        exercise.getSentences().add(first);
        exercise.getSentences().add(second);

        var result = service.checkAnswer(
                exercise,
                List.of(1L, 2L),
                List.of(2, 1)
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.displayedSentences()).containsExactly(first, second);
        assertThat(result.displayedOrders()).containsExactly(2, 1);
    }

    @Test
    void mismatchedSizeThrowsException() {
        Exercise exercise = new Exercise();

        assertThatThrownBy(() -> service.checkAnswer(
                exercise,
                List.of(1L, 2L),
                List.of(1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid sentence ids size 2");
    }

    @Test
    void unknownSentenceIdThrowsException() {
        Exercise exercise = new Exercise();

        ExerciseSentence sentence = new ExerciseSentence();
        sentence.setId(2L);
        sentence.setText("Second");
        sentence.setCorrectOrder(1);
        sentence.setExercise(exercise);
        exercise.getSentences().add(sentence);

        assertThatThrownBy(() -> service.checkAnswer(
                exercise,
                List.of(1L),
                List.of(1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sentence not found: 1");
    }
}