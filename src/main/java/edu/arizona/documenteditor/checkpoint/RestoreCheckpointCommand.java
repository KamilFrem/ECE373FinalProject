package edu.arizona.documenteditor.checkpoint;

import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Section;

import java.util.List;

/** Restores a deep checkpoint while preserving object identities for undo history. */
public final class RestoreCheckpointCommand implements EditCommand {
    private final Document document;
    private final DocumentSnapshot checkpoint;
    private List<Section> before;
    private List<Section> restored;
    private boolean executed;

    /**
     * Captures the named checkpoint for an undoable restoration.
     * @param document live document
     * @param manager checkpoint manager
     * @param checkpointName saved name
     * @throws NullPointerException if document or manager is null
     * @throws IllegalArgumentException if the checkpoint does not exist
     */
    public RestoreCheckpointCommand(Document document,
                                    CheckpointManager manager, String checkpointName) {
        if (document == null || manager == null) {
            throw new NullPointerException("Document and manager cannot be null.");
        }
        this.document = document;
        checkpoint = manager.getCheckpoint(checkpointName);
    }

    /**
     * Attaches an independent checkpoint copy, or reattaches it during redo.
     * @throws IllegalStateException if already executed
     */
    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Checkpoint restoration is already executed.");
        }
        if (restored == null) {
            Document copy = checkpoint.getDocument();
            restored = copy.getSections();
            copy.setSections(List.of());
            before = document.getSections();
        }
        document.setSections(restored);
        executed = true;
    }

    /**
     * Reattaches the original objects so older commands still address the document.
     * @throws IllegalStateException if not executed
     */
    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Execute restoration before undoing it.");
        }
        document.setSections(before);
        executed = false;
    }
}
