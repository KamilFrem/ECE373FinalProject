package edu.arizona.documenteditor.model;

import java.util.List;

/**
 * Provides the text style operations shared by paragraphs and section titles.
 * Ranges are start from 0 to end. End is exclusive. Ranges are written as
 * {@code [start, end)}. Selected ranges must not be empty: {@code start < end}.
 */
public interface FormatTarget {
    /**
     * Returns the stored text without formatting markers. For a section, this is the title only.
     * @return the text available for formatting.
     */
    String getText();

    /**
     * Returns the length of the formatting target
     * @return the length, zero for empty text
     */
    int lenght();

    /**
     * Returns the snapshot of the text runs.
     * @return the text runs
     */
    List<TextRun> getRuns();

    /**
     *  Replaces text and styles. Empty runs are removed and adjacent runs with equal styles are merged.
     *  The supplied list is copied. Invalid input leaves the current content unchanged.
     *  @param runs replacement text runs
     *  @throws NullPointerException if input is null
     *  @throws ArithmeticException if text length is greater than the integer range
     */
    void setRuns(List<TextRun> runs);

}
