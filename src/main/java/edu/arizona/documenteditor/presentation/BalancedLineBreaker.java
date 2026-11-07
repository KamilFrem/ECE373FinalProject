package edu.arizona.documenteditor.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wraps text by minimizing squared unused space across all lines.
 */
public final class BalancedLineBreaker implements LineBreaker {

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
        int count = words.length;

        for (String word : words) {
            if (word.length() > width) {
                throw new IllegalArgumentException(
                        "A word is longer than the selected width.");
            }
        }

        // bestCost[i] is the minimum cost of wrapping words i onward.
        double[] bestCost = new double[count + 1];

        // nextBreak[i] identifies the first word on the following line.
        int[] nextBreak = new int[count];

        for (int start = count - 1; start >= 0; start--) {
            bestCost[start] = Double.POSITIVE_INFINITY;
            long lineLength = 0;

            for (int end = start; end < count; end++) {
                if (end > start) {
                    lineLength++;
                }

                lineLength += words[end].length();

                if (lineLength > width) {
                    break;
                }

                double unused = width - lineLength;
                double cost = unused * unused + bestCost[end + 1];

                if (cost < bestCost[start]) {
                    bestCost[start] = cost;
                    nextBreak[start] = end + 1;
                }
            }
        }

        // Reconstruct the lines using the selected break positions.
        int start = 0;

        while (start < count) {
            int end = nextBreak[start];
            StringBuilder line = new StringBuilder();

            for (int index = start; index < end; index++) {
                if (index > start) {
                    line.append(' ');
                }

                line.append(words[index]);
            }

            lines.add(line.toString());
            start = end;
        }

        return lines;
    }
}
