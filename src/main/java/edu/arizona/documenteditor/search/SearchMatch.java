package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.model.Paragraph;

/** Stores the paragraph and character range of a search result. */
public final class SearchMatch {

    private final Paragraph paragraph;
    private final int start;
    private final int end;

    /**
     * Creates a search result.
     *
     * @param paragraph the paragraph containing the match
     * @param start     the first matching character
     * @param end       the position after the last matching character
     * @throws NullPointerException     if paragraph is null
     * @throws IllegalArgumentException if the range is invalid
     */
    public SearchMatch(Paragraph paragraph, int start, int end) {

        if(paragraph == null){

            throw new IllegalArgumentException("Paragraph cannot be null");
        }

        if (start < 0 || end <= start || end > paragraph.length()){

            throw new IllegalArgumentException("Invalid range for search result");

        }

        this.paragraph = paragraph;
        this.start = start;
        this.end = end;


    }

    /**
     * Returns the paragraph containing the match.
     *
     * @return the paragraph
     */

    public Paragraph getParagraph() {

        return paragraph;

    }

    /**
     * Returns the first matching character position.
     *
     * @return the inclusive start position
     */
    public int getStart() {

        return start;

    }


    /**
     * Returns the position after the final matching character.
     *
     * @return the exclusive end position
     */

    public int getEnd() {

        return end;

    }
}