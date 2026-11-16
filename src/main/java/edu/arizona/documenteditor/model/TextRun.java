//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;

import java.util.Objects;

/**
 * Stores immutable text with one shared style
 */
public final class TextRun {
    /** These parameters stores text and style values */
    private final String text;
    private final TextStyle style;

    /**
     * Creates a run. Empty text is permitted, null input is not permitted.
     * @param text the stored text in this run.
     * @param style the style shared by this run.
     * @throws NullPointerException if one of the inputs are null
     */
    public TextRun(final String text, final TextStyle style){
        Objects.requireNonNull(text, "text cannot be null");
        Objects.requireNonNull(style, "Style cannot be null");
        this.text = text;
        this.style = style;
    }

    /**
     * Returns the characters stored in text
     * @return the stored text
     */
    public String getText(){
        return text;
    }

    /**
     * Returns the style values applied to text
     * @return the style applied to text
     */
    public TextStyle getStyle(){
        return style;
    }

    /**
     * Returns the length of the stored text.
     * @return number of characters in text.
     */
    public int getLength() {
        return text.length();
    }

    /**
     * Compares all stored values, instead of object itself
     * @param object   the reference object with which to compare.
     * @return true when other object has same type and values
     */
    @Override
    public boolean equals(final Object object){
        if (this == object) {
            return true;
        }
        if (!(object instanceof TextRun)) {
            return false;
        }
        TextRun other = (TextRun) object;
        return this.text.equals(other.text) && this.style.equals(other.style);
    }

    /**
     * The object with same values but different initializations will return different hashcodes to make two
     * objects with same values equal object we need this override.
     * @return the value-based hashcode
     */
    @Override
    public int hashCode(){
        return Objects.hash(text, style);
    }

    /**
     * Describe the stored values for debugging
     * @return TextRun[text=storedText, style=styleValues]
     */
    @Override
    public String toString(){
        return "TextRun[text=" + text + ", style=" + style + "]";
    }
}
