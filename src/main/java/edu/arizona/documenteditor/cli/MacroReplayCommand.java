package edu.arizona.documenteditor.cli;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.checkpoint.DocumentSnapshot;
import edu.arizona.documenteditor.command.CompositeCommand;
import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.presentation.LineBreaker;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.presentation.Renderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Replays saved commands in order as one undoable operation. */
public final class MacroReplayCommand implements EditCommand {
    private final CompositeCommand group;
    private final ArrayList<ReplayStep> steps;

    /**
     * Creates a replay with deferred target lookup for each step.
     * Recording, history control, scripts, exit, and nested replay are excluded.
     * @param dispatcher shared application dispatcher
     * @param lines saved command lines
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the macro is empty or has null lines
     */
    public MacroReplayCommand(CommandDispatcher dispatcher, List<String> lines) {
        if (dispatcher == null || lines == null) {

            throw new NullPointerException("Dispatcher and commands cannot be null.");
        }

        if (lines.isEmpty()) {

            throw new IllegalArgumentException("Cannot replay an empty macro.");

        }
        steps = new ArrayList<ReplayStep>();

        ArrayList<EditCommand> commands = new ArrayList<EditCommand>();

        for (int i = 0; i < lines.size(); i++) {

            if (lines.get(i) == null) {

                throw new IllegalArgumentException("Macro contains a null command.");

            }

            ReplayStep step = new ReplayStep(dispatcher, lines.get(i));
            steps.add(step);
            commands.add(step);
        }
        group = new CompositeCommand(commands);
    }

    /** Executes the steps; completed steps roll back if a later step fails. */
    @Override
    public void execute() {
        group.execute();
    }

    /** Reverses document edits, presentation settings, and checkpoint changes. */
    @Override
    public void undo() {
        group.undo();
    }

    /** @return buffered output from the successful replay */
    public String getOutput() {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                result.append("\n");
            }
            result.append(steps.get(i).message);
        }
        return result.toString();
    }

    // One wrapper per line lets CompositeCommand execute, undo, and compensate
    // mixed commands without putting each child into HistoryManager separately.
    private static final class ReplayStep implements EditCommand {
        private final CommandDispatcher dispatcher;
        private final String line;
        private EditCommand edit;
        private State before;
        private State after;
        private String message = "";

        private ReplayStep(CommandDispatcher dispatcher, String line) {
            this.dispatcher = dispatcher;
            this.line = line;
        }

        @Override
        public void execute() {
            if (after != null) {
                if (edit != null) {
                    edit.execute();
                }
                after.restore(dispatcher);
                return;
            }
            before = new State(dispatcher);
            boolean editCompleted = false;
            try {
                ParsedCommand parsed = new CommandParser().parse(line);
                String name = parsed.getName();

                if (name.equals("record") || name.equals("stop") || name.equals("replay") || name.equals("undo") || name.equals("redo") || name.equals("script") || name.equals("exit")) {

                    throw new IllegalArgumentException("Macro cannot contain control command: " + name);
                }

                edit = dispatcher.createEditCommand(parsed);
                if (edit != null) {
                    edit.execute();
                    editCompleted = true;
                    message = dispatcher.editMessage(name);

                }
                else {
                    message = dispatcher.executeNonEdit(parsed);
                }
                after = new State(dispatcher);

            }
            catch (RuntimeException exception) {

                if (editCompleted) {
                    try {
                        edit.undo();
                    } catch (RuntimeException rollback) {
                        exception.addSuppressed(rollback);
                    }
                }
                try {
                    before.restore(dispatcher);
                } catch (RuntimeException rollback) {
                    exception.addSuppressed(rollback);
                }
                throw exception;
            }
        }

        @Override
        public void undo() {
            if (edit != null) {
                edit.undo();
            }
            before.restore(dispatcher);
        }
    }

    // This stores application settings, not a deep copy of the live document.
    // Document changes are undone by their own commands, preserving identities.
    private static final class State {
        private final Renderer renderer;
        private final LineBreaker breaker;
        private final int width;
        private final LinkedHashMap<String, DocumentSnapshot> checkpoints;

        private State(CommandDispatcher dispatcher) {
            RenderService rendering = dispatcher.rendering();
            renderer = rendering.getRenderer();
            breaker = rendering.getLineBreaker();
            width = rendering.getWidth();
            checkpoints = new LinkedHashMap<String, DocumentSnapshot>();
            CheckpointManager manager = dispatcher.checkpoints();
            for (String name : manager.getCheckpointNames()) {
                checkpoints.put(name, manager.getCheckpoint(name));
            }
        }

        private void restore(CommandDispatcher dispatcher) {
            RenderService rendering = dispatcher.rendering();
            rendering.setRenderer(renderer);
            rendering.setLineBreaker(breaker);
            rendering.setWidth(width);
            CheckpointManager manager = dispatcher.checkpoints();
            manager.clear();
            for (String name : checkpoints.keySet()) {
                manager.createCheckpoint(name, checkpoints.get(name).getDocument());
            }
        }
    }
}
