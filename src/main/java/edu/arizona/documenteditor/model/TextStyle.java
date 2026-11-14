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
        return new TextStyle(value, italic, fontName, fontSize, color);
    }

    /**
     * Creates style with requested italic setting.
     * @param value the new italic setting
     * @return a new generated style
     */
    public TextStyle withItalic(final boolean value){
        return new TextStyle(bold, value, fontName, fontSize, color);
    }
    /**
     * Creates style with requested fontName setting.
     * @param value the new fontName setting
     * @return a new generated style
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if input String is blank
     */
    public TextStyle withFontName(final String value){
        return new TextStyle(bold, italic, value, fontSize, color);
    }
    /**
     * Creates style with requested size setting.
     * @param value the new size setting
     * @return a new generated style
     * @throws IllegalArgumentException if the size of the font is not positive
     */
    public TextStyle withFontSize(final int value){
        return new TextStyle(bold, italic, fontName, value, color);
    }
    /**
     * Creates style with requested color setting.
     * @param value the new color setting
     * @return a new generated style
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if input String is blank
     */
    public TextStyle withColor(final String value){
        return new TextStyle(bold, italic, fontName, fontSize, value);
    }

    /**
     * Returns if text is bold
     * @return bold value
     */
    public boolean isBold(){
        return bold;
    }

    /**
     * Returns if text is italic
     * @return italic value
     */
    public boolean isItalic(){
        return italic;
    }

    /**
     * Returns font name
     * @return fontName value
     */
    public String getFontName(){
        return fontName;
    }

    /**
     * Returns size of the font
     * @return fontSize value
     */
    public int getFontSize(){
        return fontSize;
    }

    /**
     * Returns texts color
     * @return color value
     */
    public String getColor(){
        return color;
    }

    /**
     * Compares all stored values, instead of object itself
     * @param object   the reference object with which to compare.
     * @return true when other object has same type and values
     */
    @Override
    public boolean equals(final Object object){
        if (this == object){
            return true;
        }
        if(!(object instanceof TextStyle)){
            return false;
        }
        TextStyle other = (TextStyle) object;
        return this.bold == other.bold && this.italic == other.italic
                && this.fontName.equals(other.fontName) && this.fontSize == other.fontSize
                && this.color.equals(other.color);
    }

    /**
     * The object with same values but different initializations will return different hashcodes to make two
     * objects with same values equal object we need this override.
     * @return the value-based hashcode
     */
    @Override
    public int hashCode(){
        return Objects.hash(bold, italic, fontName, fontSize, color);
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
