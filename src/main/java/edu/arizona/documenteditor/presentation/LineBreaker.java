package edu.arizona.documenteditor.presentation;

import java.util.List;

/**
 * Defines how paragraph text is wrapped into lines.
 */
public interface LineBreaker {

    /**
     * Wraps text using the requested line width.
     *
     * @param text the text to wrap
     * @param width the maximum characters per line
     * @return the wrapped lines
     * @throws NullPointerException if text is null
     * @throws IllegalArgumentException if width is not positive
     *         or a word is longer than width
     */
    List<String> breakLines(String text, int width);


}
