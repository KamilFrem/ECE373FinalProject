package edu.arizona.documenteditor.checkpoint;

import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.model.Document;

import java.util.Objects;

/**
 * Restores a document from a checkpoint.
 * The restoration itself can be undone and redone.
 */
public final class RestoreCheckpointCommand implements EditCommand {

    private final Document document;
    private final DocumentSnapshot checkpoint;

    private DocumentSnapshot previousState;
    private boolean executed;

    public RestoreCheckpointCommand(
            Document document,
            CheckpointManager manager,
            String checkpointName) {

        this.document = Objects.requireNonNull(
                document, "Document cannot be null.");

        Objects.requireNonNull(manager, "Checkpoint manager cannot be null.");

        // Capture the checkpoint now so this command remains usable
        // even if the named checkpoint is later removed.
        this.checkpoint = manager.getCheckpoint(checkpointName);
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException(
                    "Checkpoint restoration is already executed.");
        }

        // Capture the current state only on the first execution.
        if (previousState == null) {
            previousState = new DocumentSnapshot(document);
        }

        document.restore(checkpoint.getDocument());
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed || previousState == null) {
            throw new IllegalStateException(
                    "Cannot undo a restoration that was not executed.");
        }

        document.restore(previousState.getDocument());
        executed = false;
    }
}
