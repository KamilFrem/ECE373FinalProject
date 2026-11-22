package edu.arizona.documenteditor.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Runs the same commands as the console from a UTF-8 script file. */
public final class ScriptRunner {
    private final CommandDispatcher dispatcher;

    /**
     * Creates a script runner.
     * @param dispatcher shared command dispatcher
     * @throws NullPointerException if dispatcher is null
     */
    public ScriptRunner(CommandDispatcher dispatcher) {
        if (dispatcher == null) {
            throw new NullPointerException("Dispatcher cannot be null.");
        }
        this.dispatcher = dispatcher;
    }

    /**
     * Runs a script, skipping blank lines and full-line hash comments.
     * Stops on the first error; earlier successful commands remain applied.
     * Nested scripts and exit commands are rejected inside scripts.
     * @param path script file path
     * @return collected output, or an error with its line number
     */
    public CommandResult run(String path) {
        StringBuilder output = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Path.of(path), StandardCharsets.UTF_8);
            CommandParser parser = new CommandParser();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0 && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                if (line.isBlank() || line.stripLeading().startsWith("#")) {
                    continue;
                }
                CommandResult result;
                try {
                    String name = parser.parse(line).getName();
                    if (name.equals("script") || name.equals("exit")) {
                        throw new IllegalArgumentException(
                                "Nested scripts and exit are not allowed inside scripts.");
                    }
                    result = dispatcher.dispatch(line);
                } catch (RuntimeException exception) {
                    result = new CommandResult(false, exception.getMessage());
                }
                if (!result.isSuccess()) {
                    return new CommandResult(false, output + "Line " + (i + 1)
                            + ": " + result.getMessage());
                }
                output.append(result.getMessage()).append("\n");
            }
            if (output.length() > 0) {
                output.setLength(output.length() - 1);
            }
            return new CommandResult(true, output.toString());
        } catch (IOException exception) {
            return new CommandResult(false, "Cannot read script: " + exception.getMessage());
        } catch (RuntimeException exception) {
            return new CommandResult(false, "Invalid script path: " + exception.getMessage());
        }
    }
}
