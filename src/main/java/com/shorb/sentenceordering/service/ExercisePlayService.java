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

    public List<SentenceAnswerFeedback> uncheckedSentenceFeedback(List<ExerciseSentence> sentences) {
        return sentences.stream()
                .map(sentence -> new SentenceAnswerFeedback(sentence, null))
                .toList();
    }

    public ExerciseAnswerResult checkAnswer(
            Exercise exercise,
            List<Long> sentenceIds
    ) {
        if (sentenceIds.size() != exercise.getSentences().size()) {
            throw new IllegalArgumentException("Invalid sentence ids size " + sentenceIds.size());
        }

        List<SentenceAnswerFeedback> displayedSentences = new ArrayList<>();

        boolean correct = true;

        for (int i = 0; i < sentenceIds.size(); i++) {
            Long sentenceId = sentenceIds.get(i);
            int studentOrder = i + 1;

            ExerciseSentence sentence = exercise.getSentences()
                    .stream()
                    .filter(s -> s.getId().equals(sentenceId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Sentence not found: " + sentenceId));

            boolean correctPosition = sentence.getCorrectOrder() == studentOrder;

            displayedSentences.add(new SentenceAnswerFeedback(sentence, correctPosition));

            if (!correctPosition){
                correct = false;
            }
        }
        if (correct) {
            return new ExerciseAnswerResult(true, exercise.getSentences()
                    .stream()
                    .map(sentence -> new SentenceAnswerFeedback(sentence, true))
                    .toList());
        }

        return new ExerciseAnswerResult(false, displayedSentences);
    }

    public record SentenceAnswerFeedback(
            ExerciseSentence sentence,
            Boolean correctPosition
    ) {
    }

    public record ExerciseAnswerResult(
            boolean correct,
            List<SentenceAnswerFeedback> displayedSentences
    ) {
    }
}
