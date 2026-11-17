package edu.arizona.documenteditor.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Groups multiple edit commands into one undoable operation.
 */
public final class CompositeCommand implements EditCommand {

    private final List<EditCommand> commands;
    private boolean executed;

    public CompositeCommand(List<EditCommand> commands) {
        Objects.requireNonNull(commands, "Commands cannot be null.");

        if (commands.isEmpty()) {
            throw new IllegalArgumentException(
                    "A composite command must contain at least one command.");
        }

        this.commands = List.copyOf(commands);

        for (EditCommand command : this.commands) {
            Objects.requireNonNull(command, "Command cannot be null.");
        }
    }

    public CompositeCommand(EditCommand... commands) {
        this(Arrays.asList(commands));
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Composite command is already executed.");
        }

        int completed = 0;

        try {
            for (EditCommand command : commands) {
                command.execute();
                completed++;
            }
        } catch (RuntimeException exception) {
            // Roll back every child command that completed successfully.
            for (int i = completed - 1; i >= 0; i--) {
                try {
                    commands.get(i).undo();
                } catch (RuntimeException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
            }

            throw exception;
        }

        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Composite command has not been executed.");
        }

        List<EditCommand> undone = new ArrayList<>();

        try {
            for (int i = commands.size() - 1; i >= 0; i--) {
                EditCommand command = commands.get(i);
                command.undo();
                undone.add(command);
            }
        } catch (RuntimeException exception) {
            // Restore the state that existed before undo began.
            for (int i = undone.size() - 1; i >= 0; i--) {
                try {
                    undone.get(i).execute();
                } catch (RuntimeException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
            }

            throw exception;
        }

        executed = false;
    }

    public List<EditCommand> getCommands() {
        return List.copyOf(commands);
    }
}
