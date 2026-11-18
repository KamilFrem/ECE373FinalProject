package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;

/** Converts a document into text for display. */
public interface Renderer {

    /**
     * Renders the document using the supplied wrapping strategy.
     *
     * @param document the document to display
     * @param lineBreaker the strategy used to wrap text
     * @param width the maximum text characters per line
     * @return the rendered text
     * @throws NullPointerException if document or lineBreaker is null
     * @throws IllegalArgumentException if width is not positive
     *         or a word is longer than width
     */
    String render(Document document, LineBreaker lineBreaker, int width);
}
