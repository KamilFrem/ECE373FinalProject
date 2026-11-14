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

    }
    @Test
    @DisplayName("String conversion works correctly")
    void toStringReturnsCorrectValues(){

    }

}
