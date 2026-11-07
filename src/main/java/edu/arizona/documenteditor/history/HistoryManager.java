package edu.arizona.documenteditor.history;

import edu.arizona.documenteditor.command.EditCommand;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Manages the undo and redo history for document editing commands.
 *
 * Commands are only added to history after successful execution.
 */
public final class HistoryManager {

    private final Deque<EditCommand> undoStack;
    private final Deque<EditCommand> redoStack;

    /**
     * Creates an empty history manager.
     */
    public HistoryManager() {
        undoStack = new ArrayDeque<>();
        redoStack = new ArrayDeque<>();
    }

    /**
     * Executes a command and records it in the undo history.
     *
     * A successful new command clears the redo history.
     *
     * @param command command to execute
     */
    public void executeCommand(EditCommand command) {
        Objects.requireNonNull(command, "Command cannot be null.");

        // Execute before modifying history.
        // If execution fails, neither stack is changed.
        command.execute();

        undoStack.push(command);

        // A new successful edit invalidates the previous redo path.
        redoStack.clear();
    }

    /**
     * Undoes the most recently executed command.
     *
     * @throws IllegalStateException if there is nothing to undo
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("There is no command to undo.");
        }

        EditCommand command = undoStack.peek();

        // Only move the command if undo succeeds.
        command.undo();

        undoStack.pop();
        redoStack.push(command);
    }

    /**
     * Redoes the most recently undone command.
     *
     * @throws IllegalStateException if there is nothing to redo
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("There is no command to redo.");
        }

        EditCommand command = redoStack.peek();

        // Only move the command if execution succeeds.
        command.execute();

        redoStack.pop();
        undoStack.push(command);
    }

    /**
     * Checks whether an undo operation is available.
     *
     * @return true if a command can be undone
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks whether a redo operation is available.
     *
     * @return true if a command can be redone
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Gets the number of commands available to undo.
     *
     * @return undo stack size
     */
    public int undoCount() {
        return undoStack.size();
    }

    /**
     * Gets the number of commands available to redo.
     *
     * @return redo stack size
     */
    public int redoCount() {
        return redoStack.size();
    }

    /**
     * Clears all undo and redo history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}