package edu.arizona.documenteditor.checkpoint;

import edu.arizona.documenteditor.model.Document;

import java.util.Objects;

/**
 * Stores an independent snapshot of a document.
 */
public final class DocumentSnapshot {

    private final Document snapshot;

    public DocumentSnapshot(Document document) {
        Objects.requireNonNull(document, "Document cannot be null.");
        this.snapshot = document.deepCopy();
    }

    /**
     * Returns an independent copy of the saved document.
     */
    public Document getDocument() {
        return snapshot.deepCopy();
    }
}
