package edu.arizona.documenteditor.presentation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GreedyLineBreakerTest {

    private final LineBreaker breaker = new GreedyLineBreaker();

    @Test
    void wrapsWordsAtSelectedWidth() {
        assertEquals(
                List.of("Hello world", "from Java"),
                breaker.breakLines("Hello world from Java", 11));
    }

    @Test
    void keepsWordsThatExactlyFit() {
        assertEquals(
                List.of("one two", "six"),
                breaker.breakLines("one two six", 7));
    }

    @Test
    void returnsNoLinesForBlankText() {
        assertEquals(List.of(), breaker.breakLines("   ", 10));
    }

    @Test
    void rejectsInvalidWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> breaker.breakLines("Hello", 0));
    }

    @Test
    void rejectsWordLongerThanWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> breaker.breakLines("Hello", 3));
    }

    @Test
    void returnsNoLinesForEmptyText() {
        assertEquals(List.of(), breaker.breakLines("", 10));
    }

    @Test
    void rejectsNullText() {
        assertThrows(
                NullPointerException.class,
                () -> breaker.breakLines(null, 10));
    }

    @Test
    void rejectsNegativeWidth() {
        assertThrows(
                IllegalArgumentException.class,
                () -> breaker.breakLines("Hello", -1));
    }

    @Test
    void wrapsAtWidthOne() {
        assertEquals(
                List.of("a", "b", "c"),
                breaker.breakLines("a b c", 1));
    }

    @Test
    void normalizesExtraWhitespace() {
        assertEquals(
                List.of("Hello world"),
                breaker.breakLines("  Hello\t  world  ", 11));
    }
}