package edu.arizona.documenteditor.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps text by minimizing the sum of squared unused spaces on all lines.
 */
public final class BalancedLineBreaker implements LineBreaker {

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

        // Split the text at spaces, tabs, or other whitespace.
        String[] words = text.strip().split("\\s+");
        int wordCount = words.length;

        for (int i = 0; i < wordCount; i++) {
            if (words[i].length() > width) {
                throw new IllegalArgumentException("A word is longer than the selected width.");
            }
        }

        // bestCost[i] is the lowest cost for wrapping from word i to the end.
        double[] bestCost = new double[wordCount + 1];

        // nextLineStart[i] is where the next line starts if this line starts at i.
        int[] nextLineStart = new int[wordCount];

        // There is no cost when there are no words left.
        bestCost[wordCount] = 0;

        for (int start = wordCount - 1; start >= 0; start--) {
            bestCost[start] = Double.POSITIVE_INFINITY;
            long lineLength = 0;

            for (int end = start; end < wordCount; end++) {
                // Count the space before each word after the first one.
                if (end > start) {
                    lineLength = lineLength + 1;
                }

                lineLength = lineLength + words[end].length();

                if (lineLength > width) {
                    break;
                }

                double unusedSpace = width - lineLength;
                double lineCost = unusedSpace * unusedSpace;
                double totalCost = lineCost + bestCost[end + 1];

                if (totalCost < bestCost[start]) {
                    bestCost[start] = totalCost;
                    nextLineStart[start] = end + 1;
                }
            }
        }

        // Start at the first word and follow the saved line breaks.
        int start = 0;

        while (start < wordCount) {
            int nextStart = nextLineStart[start];
            StringBuilder line = new StringBuilder();

            for (int i = start; i < nextStart; i++) {
                if (i > start) {
                    line.append(" ");
                }

                line.append(words[i]);
            }

            lines.add(line.toString());
            start = nextStart;
        }

        return lines;
    }
}