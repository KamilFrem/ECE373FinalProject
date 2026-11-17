package edu.arizona.documenteditor.presentation;

import java.util.ArrayList;
import java.util.List;

/** Wraps text by fitting as many complete words as possible on each line. */
public final class GreedyLineBreaker implements LineBreaker {

    /** {@inheritDoc} */
    @Override
    public List<String> breakLines(String text, int width) {
        if (text == null) {
            throw new NullPointerException("Text cannot be null.");
        }

        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive.");
        }

        List<String> lines = new ArrayList<>();

        if (text.isBlank()) {
            return lines;
        }

        String[] words = text.strip().split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (word.length() > width) {
                throw new IllegalArgumentException( "A word is longer than the selected width.");
            }

            if (currentLine.length() > 0) {
                // Count the space between the current text and the next word.
                long newLength = currentLine.length();
                newLength = newLength + 1 + word.length();

                if (newLength > width) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }
            }

            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }

            currentLine.append(word);
        }

        // The loop ends with the last line still in currentLine.
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}