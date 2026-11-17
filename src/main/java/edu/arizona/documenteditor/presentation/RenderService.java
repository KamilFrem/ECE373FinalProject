package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;

/** Stores the settings used to display a document. */
public final class RenderService {
    private Renderer renderer;
    private LineBreaker lineBreaker;
    private int width;

    /** Creates a service with the default rendering settings. */
    public RenderService() {
        renderer = new PlainTextRenderer();
        lineBreaker = new GreedyLineBreaker();
        width = 80;
    }

    /**
     * Changes how the document is displayed.
     *
     * @param renderer the renderer to use
     */
    public void setRenderer(Renderer renderer) {
        if (renderer == null) {
            throw new NullPointerException("Renderer cannot be null.");
        }

        this.renderer = renderer;
    }

    /**
     * Changes how text is wrapped.
     *
     * @param lineBreaker the line breaker to use
     */
    public void setLineBreaker(LineBreaker lineBreaker) {
        if (lineBreaker == null) {
            throw new NullPointerException("Line breaker cannot be null.");
        }

        this.lineBreaker = lineBreaker;
    }

    /**
     * Changes the maximum line width.
     *
     * @param width the new width in characters
     */
    public void setWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive.");
        }

        this.width = width;
    }

    /**
     * Renders a document using the current settings.
     *
     * @param document the document to display
     * @return the rendered text
     */
    public String render(Document document) {
        return renderer.render(document, lineBreaker, width);
    }
}
