package edu.arizona.documenteditor.command;

/**
 * Represents an operation that modifies a document and can be undone.
 * Each command is responsible for preserving the information needed
 * to reverse its own changes.
 */
public interface EditCommand {

    /**
     * Executes this command.
     *
     * @throws RuntimeException if the operation cannot be completed
     */
    void execute();

    /**
     * Reverses the effects of this command.
     *
     * @throws RuntimeException if the operation cannot be undone
     */
    void undo();
}
