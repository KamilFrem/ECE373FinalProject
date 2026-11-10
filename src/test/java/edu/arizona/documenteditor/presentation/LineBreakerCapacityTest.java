package edu.arizona.documenteditor.presentation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineBreakerCapacityTest {

    @Test
    void greedyHandlesLargeParagraph() {
        verifyLargeParagraph(new GreedyLineBreaker());
    }

    @Test
    void balancedHandlesLargeParagraph() {
        verifyLargeParagraph(new BalancedLineBreaker());
    }

    private void verifyLargeParagraph(LineBreaker breaker) {
        // Creates a paragraph containing more than 100,000 characters.
        String text = "alpha beta gamma delta ".repeat(5000).strip();
        int width = 50;

        List<String> lines = breaker.breakLines(text, width);

        assertTrue(text.length() >= 100_000);
        assertTrue(!lines.isEmpty());

        // Wrapping must preserve every word and its original order.
        assertEquals(text, String.join(" ", lines));

        for (String line : lines) {
            assertTrue(
                    line.length() <= width,
                    "Line exceeds the selected width: " + line);

            assertTrue(!line.isBlank(), "Unexpected blank line.");
        }
    }
}