package edu.ucsd.cse.eulexia;

/**
 * Created by phoebe on 11/17/15.
 */
public class Suggestion implements Comparable<Suggestion> {
    public String word;
    public int priority;

    public Suggestion(String word, int priority) {
        this.word = word;
        this.priority = priority;
    }

    @Override
    public int compareTo(Suggestion otherSuggestion) {

        return priority - otherSuggestion.priority;
    }
}
