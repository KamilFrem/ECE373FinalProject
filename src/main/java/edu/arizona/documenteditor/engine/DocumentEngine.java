package edu.arizona.documenteditor.engine;

import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.history.HistoryManager;
import edu.arizona.documenteditor.model.Document;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordinates document editing, undo/redo history, and notifications.
 */
public final class DocumentEngine {

    private final Document document;
    private final HistoryManager historyManager;
    private final CopyOnWriteArrayList<DocumentChangeListener> listeners;

    /**
     * Creates an engine around an existing document.
     */
    public DocumentEngine(Document document) {
        this.document = Objects.requireNonNull(
                document, "Document cannot be null.");

        this.historyManager = new HistoryManager();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Creates an engine with a new empty document.
     */
    public DocumentEngine() {
        this(new Document());
    }

    /**
     * Executes a command and records it in history.
     */
    public void execute(EditCommand command) {
        historyManager.executeCommand(command);
        notifyListeners();
    }

    /**
     * Undoes the most recent command.
     */
    public void undo() {
        historyManager.undo();
        notifyListeners();
    }

    /**
     * Redoes the most recently undone command.
     */
    public void redo() {
        historyManager.redo();
        notifyListeners();
    }

    /**
     * Returns the document managed by this engine.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the history manager.
     */
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    /**
     * Registers a document change listener.
     */
    public void addChangeListener(DocumentChangeListener listener) {
        listeners.add(Objects.requireNonNull(
                listener, "Listener cannot be null."));
    }

    /**
     * Removes a registered document change listener.
     *
     * @return true if the listener was registered and removed
     */
    public boolean removeChangeListener(DocumentChangeListener listener) {
        return listeners.remove(Objects.requireNonNull(
                listener, "Listener cannot be null."));
    }

    /**
     * Returns whether an undo operation is available.
     */
    public boolean canUndo() {
        return historyManager.canUndo();
    }

    /**
     * Returns whether a redo operation is available.
     */
    public boolean canRedo() {
        return historyManager.canRedo();
    }

    private void notifyListeners() {
        for (DocumentChangeListener listener : listeners) {
            listener.onDocumentChanged(document);
        }
    }
}
