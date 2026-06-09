package com.shorb.sentenceordering.service;

import com.shorb.sentenceordering.model.Exercise;
import com.shorb.sentenceordering.model.ExerciseSentence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ExercisePlayService {

    public List<ExerciseSentence> shuffledSentences(Exercise exercise) {
        List<ExerciseSentence> shuffledSentences = new ArrayList<>(exercise.getSentences());
        Collections.shuffle(shuffledSentences);
        return shuffledSentences;
    }

    public ExerciseAnswerResult checkAnswer(
            Exercise exercise,
            List<Long> sentenceIds,
            List<Integer> orders
    ) {
        if (sentenceIds.size() != orders.size()) {
            throw new IllegalArgumentException("Invalid sentence ids size " + sentenceIds.size());
        }

        List<ExerciseSentence> displayedSentences = new ArrayList<>();
        List<Integer> displayedOrders = new ArrayList<>();

        boolean correct = true;

        for (int i = 0; i < sentenceIds.size(); i++) {
            Long sentenceId = sentenceIds.get(i);
            Integer studentOrder = orders.get(i);

            ExerciseSentence sentence = exercise.getSentences()
                    .stream()
                    .filter(s -> s.getId().equals(sentenceId))
                    .findFirst()
                    .orElseThrow();

            displayedSentences.add(sentence);
            displayedOrders.add(studentOrder);

            if (sentence.getCorrectOrder() != studentOrder){
                correct = false;
            }
        }
        if (correct) {
            return new ExerciseAnswerResult(true, exercise.getSentences(), null);
        }

        return new ExerciseAnswerResult(false, displayedSentences, displayedOrders);
    }

    public record ExerciseAnswerResult(
            boolean correct,
            List<ExerciseSentence> displayedSentences,
            List<Integer> displayedOrders
    ) {
    }
}
