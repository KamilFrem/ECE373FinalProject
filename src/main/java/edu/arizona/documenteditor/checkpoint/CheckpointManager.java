package edu.arizona.documenteditor.checkpoint;

import edu.arizona.documenteditor.model.Document;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Stores and manages named document checkpoints.
 */
public final class CheckpointManager {

    private final Map<String, DocumentSnapshot> checkpoints =
            new LinkedHashMap<>();

    /**
     * Saves the current document state under a name.
     * Reusing a name replaces the previous checkpoint.
     */
    public void createCheckpoint(String name, Document document) {
        validateName(name);
        Objects.requireNonNull(document, "Document cannot be null.");

        checkpoints.put(name, new DocumentSnapshot(document));
    }

    /**
     * Returns whether a checkpoint exists.
     */
    public boolean hasCheckpoint(String name) {
        validateName(name);
        return checkpoints.containsKey(name);
    }

    /**
     * Returns the named checkpoint.
     */
    public DocumentSnapshot getCheckpoint(String name) {
        validateName(name);

        DocumentSnapshot snapshot = checkpoints.get(name);

        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "Checkpoint does not exist: " + name);
        }

        return snapshot;
    }

    /**
     * Removes a checkpoint.
     *
     * @return true if a checkpoint was removed
     */
    public boolean removeCheckpoint(String name) {
        validateName(name);
        return checkpoints.remove(name) != null;
    }

    /**
     * Returns the names of all saved checkpoints.
     */
    public Set<String> getCheckpointNames() {
        return Collections.unmodifiableSet(checkpoints.keySet());
    }

    /**
     * Returns the number of saved checkpoints.
     */
    public int checkpointCount() {
        return checkpoints.size();
    }

    /**
     * Removes every saved checkpoint.
     */
    public void clear() {
        checkpoints.clear();
    }

    private void validateName(String name) {
        Objects.requireNonNull(name, "Checkpoint name cannot be null.");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Checkpoint name cannot be blank.");
        }
    }
}
