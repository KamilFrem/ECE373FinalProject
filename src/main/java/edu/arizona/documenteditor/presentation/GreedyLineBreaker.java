package edu.arizona.documenteditor.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wraps text by fitting as many complete words as possible on each line.
 */
public final class GreedyLineBreaker implements LineBreaker {

    @Override
    public List<String> breakLines(String text, int width) {
        Objects.requireNonNull(text, "Text cannot be null.");

        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive.");
        }

        List<String> lines = new ArrayList<>();

        if (text.isBlank()) {
            return lines;
        }

        String[] words = text.strip().split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (word.length() > width) {
                throw new IllegalArgumentException(
                        "A word is longer than the selected width.");
            }

            // Include the separating space when checking whether the word fits.
            if (currentLine.length() > 0
                    && currentLine.length() + 1 + word.length() > width) {
                lines.add(currentLine.toString());
                currentLine.setLength(0);
            }

            if (currentLine.length() > 0) {
                currentLine.append(' ');
            }

            currentLine.append(word);
        }

        // Save the final line, which has no following word to trigger a flush.
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
