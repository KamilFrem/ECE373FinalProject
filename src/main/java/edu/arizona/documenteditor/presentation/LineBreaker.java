package edu.arizona.documenteditor.presentation;

import java.util.List;

/**
 * Defines how paragraph text is wrapped into lines.
 */
public interface LineBreaker {

    /**
     * Wraps paragraph text using the requested line width.
     *
     * text paragraph text to wrap
     * width maximum characters per line
     * return the resulting lines
     */
    List<String> breakLines(String text, int width);


}
