//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies immutable text runs, their validation, and value for different objects to be equal.
 */
class TextRunTest {
    @Test
    @DisplayName("non-null arguments returns correct values")
    void textRunGetterAndSetterReturnsCorrectValues(){
        TextStyle style = new TextStyle( false, false, "Arial", 12, "black").withBold(true);
        TextRun run = new TextRun("Hello", style);
        assertEquals("Hello", run.getText());
        assertEquals(5, run.getLength());
        assertSame(style, run.getStyle());
    }
    @Test
    @DisplayName("Independently created text runs are equal if their values are equal")
    void textRunsHaveValueEqualityAndConsistentHashes(){
        TextStyle DEFAULT = new TextStyle( false, false, "Arial", 12, "black");
        TextRun first = new TextRun("abc", DEFAULT);
        TextRun second = new TextRun("abc", new TextStyle( false, false, "Arial", 12, "black"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        assertEquals(1, new HashSet<>(List.of(first,second)).size());

        assertNotEquals(first, new TextRun("abcd", DEFAULT));
        assertNotEquals(first, new TextRun("abc",DEFAULT.withBold(true)));
    }
    @Test
    @DisplayName("TextRun does not accept null text and style")
    void rejectNullArguments(){

    }
    @Test
    @DisplayName("TextRun accepts empty text")
    void acceptsEmptyText(){

    }
    @Test
    @DisplayName("Text run stores and outputs correct text and textStyle")
    void toStringOuptutsCorrectValues(){

    }
}
