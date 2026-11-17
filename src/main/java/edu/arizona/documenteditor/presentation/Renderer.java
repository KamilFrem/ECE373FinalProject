package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;

/** Converts a document into text for display. */
public interface Renderer {

    /**
     * Renders the document using the supplied wrapping strategy.
     *
     * Document is what we will display
     * lineBreaker is what tell us how we will break
     * param will tell the maximum line width
     * We will return the rendered text
     */
    String render(Document document, LineBreaker lineBreaker, int width);
}
