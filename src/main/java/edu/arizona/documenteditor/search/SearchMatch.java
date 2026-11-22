package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.model.FormatTarget;
import edu.arizona.documenteditor.model.Paragraph;

/** Stores a search result in a paragraph or section title. */
public final class SearchMatch {

    private final FormatTarget target;
    private final int start;
    private final int end;

    /**
     * Creates a search result.
     *
     * @param target the paragraph or section containing the match
     * @param start the first matching character
     * @param end the position after the last matching character
     * @throws NullPointerException if target is null
     * @throws IllegalArgumentException if the range is invalid
     */
    public SearchMatch(FormatTarget target, int start, int end) {
        if (target == null) {

            throw new NullPointerException("Search target cannot be null.");
        }

        if (start < 0 || end <= start || end > target.length()) {

            throw new IllegalArgumentException("Invalid search result range.");
        }

        this.target = target;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the paragraph or section containing the match.
     *
     * @return the matching text target
     */
    public FormatTarget getTarget() {

        return target;

    }

    /**
     * Returns the paragraph when this is a paragraph match.
     *
     * @return the paragraph, or null for a section-title match
     */
    public Paragraph getParagraph() {
        if (target.getClass() == Paragraph.class) {

            Paragraph paragraph = (Paragraph) target;

            return paragraph;

        }

        return null;
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
     * Returns the position after the last matching character.
     *
     * @return the exclusive end position
     */
    public int getEnd() {

        return end;

    }
}