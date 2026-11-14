//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;

import java.util.Objects;

/**
 * Stores immutable formatting values that can be shared by runs
 */
public final class TextStyle {
    /**
     * bold, italic, font name, color and fontsize is stored in following variables
     */
    private final boolean bold;
    private final boolean italic;
    private final String fontName;
    private final String color;
    private final int fontSize;


    /**
     * Creates abd validates an immutable style
     * Font names and colors are stored as nonblank strings.
     * @param bold stores bold enabled status
     * @param italic stores italic enabled status
     * @param fontName stores font name. Cannot be empty or null
     * @param fontSize stores font size.
     * @param color stores color of the font. Cannot be empty or null
     * @throws NullPointerException if a required argument is null
     * @throws IllegalArgumentException if font name or color is blank or size is not positive.
     */
    public TextStyle(final boolean bold, final boolean italic, final String fontName, final int fontSize, final String color){
        Objects.requireNonNull(fontName, "Font name cannot be null.");
        Objects.requireNonNull(color, "Color cannot be null");
        if (fontName.isBlank() || color.isBlank() || fontSize <= 0){
            throw new IllegalArgumentException("Font and color cannot be blank; size must be positive");
        }
        this.bold = bold;
        this.italic = italic;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.color = color;
    }

    /**
     * Creates style with requested bold setting
     * @param value the new bold setting
     * @return new generated style
     */
    public TextStyle withBold(final boolean value) {

    }

    /**
     * Creates style with requested italic setting.
     * @param value the new italic setting
     * @return a new generated style
     */
    public TextStyle withItalic(final boolean value){

    }
    /**
     * Creates style with requested fontName setting.
     * @param value the new fontName setting
     * @return a new generated style
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if input String is blank
     */
    public TextStyle withFontName(final String value){

    }
    /**
     * Creates style with requested size setting.
     * @param value the new size setting
     * @return a new generated style
     * @throws IllegalArgumentException if the size of the font is not positive
     */
    public TextStyle withFontSize(final int value){

    }
    /**
     * Creates style with requested color setting.
     * @param value the new color setting
     * @return a new generated style
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if input String is blank
     */
    public TextStyle withColor(final String value){

    }

    /**
     * Returns if text is bold
     * @return bold value
     */
    public boolean isBold(){}

    /**
     * Returns if text is italic
     * @return italic value
     */
    public boolean isItalic(){}

    /**
     * Returns font name
     * @return fontName value
     */
    public String getFontName(){}

    /**
     * Returns size of the font
     * @return fontSize value
     */
    public int getFontSize(){}

    /**
     * Returns texts color
     * @return color value
     */
    public String getColor(){}

    /**
     * Compares all stored values, instead of object itself
     * @param object   the reference object with which to compare.
     * @return true when other object has same type and values
     */
    @Override
    public boolean equals(final Object object){

    }

    /**
     * The object with same values but different initializations will return different hashcodes to make two
     * objects with same values equal object we need this override.
     * @return the value-based hashcode
     */
    @Override
    public int hashCode(){
    }

    /**
     * Describe the stored values for debugging
     * @return TextStyle[bold= boldValue, italic= ItalicValue, fontName= fontNameValue,
     * fontSize= fontSizeValue, color= colorValue]
     */
    @Override
    public String toString(){

    }

}
