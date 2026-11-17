package edu.arizona.documenteditor.engine;

import edu.arizona.documenteditor.model.Document;

/**
 * Receives notifications after successful document changes.
 */
@FunctionalInterface
public interface DocumentChangeListener {

    /**
     * Called after a successful edit, undo, or redo.
     */
    void onDocumentChanged(Document document);
}
