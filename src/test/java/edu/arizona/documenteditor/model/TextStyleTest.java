//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies immutable style values and input validation.
 */
public class TextStyleTest {
    @Test
    @DisplayName("Changing one attribute should not change the original textStyle.")
    void changeOneAttributeWithoutMutatingOriginal(){
        TextStyle s = new TextStyle(true, false, "Arial", 14, "red");

        assertFalse(s.withBold(false).isBold());
        assertTrue(s.isBold());

        assertTrue(s.withItalic(true).isItalic());
        assertFalse(s.isItalic());

        assertEquals("blue", s.withColor("blue").getColor());
        assertEquals("red", s.getColor());

        assertEquals("TimesNewRoman", s.withFontName("TimesNewRoman").getFontName());
        assertEquals("Arial", s.getFontName());

        assertEquals(12, s.withFontSize(12).getFontSize());
        assertEquals(14, s.getFontSize());
    }

    @Test
    @DisplayName("Text styles should only use values for equality.")
    void stylesUseValueEquality(){
        TextStyle s = new TextStyle(true, false, "Arial", 14, "red");
        TextStyle a = new TextStyle(true, false, "Arial", 14, "red");
        assertEquals(a, s);
        assertEquals(a.hashCode(), s.hashCode());
    }
    @Test
    @DisplayName("Class rejects invalid styles.")
    void rejectsInvalidStyles() {
        TextStyle s = new TextStyle(true, false, "Arial", 14, "red");
        assertThrows(IllegalArgumentException.class,() -> {s.withFontSize(0);});
        assertThrows(IllegalArgumentException.class,() -> {s.withFontSize(-12);});
        assertThrows(IllegalArgumentException.class,() -> {s.withFontName("");});
        assertThrows(IllegalArgumentException.class,() -> {s.withColor("");});
        assertThrows(NullPointerException.class, () -> {s.withColor(null);});
        assertThrows(NullPointerException.class, () -> {s.withFontName(null);});
        assertThrows(IllegalArgumentException.class,() -> {new TextStyle(true, false, "", 14, "red");});
        assertThrows(NullPointerException.class,() -> {new TextStyle(true, false, "Ar", 14, null);});
    }
    @Test
    @DisplayName("String conversion works correctly")
    void toStringReturnsCorrectValues(){
        TextStyle s = new TextStyle(true, false, "Arial", 14, "red");
        TextStyle a = new TextStyle(false, true, "testFont", 12, "blue");
        String result1 = "TextStyle[bold=true, italic=false, fontName=Arial, fontSize=14, color=red]";
        String result2 = "TextStyle[bold=false, italic=true, fontName=testFont, fontSize=12, color=blue]";
        assertEquals(result1, s.toString());
        assertEquals(result2, a.toString());
    }

}
