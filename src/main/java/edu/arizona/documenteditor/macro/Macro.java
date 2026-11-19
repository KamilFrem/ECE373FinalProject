package edu.arizona.documenteditor.macro;

import java.util.ArrayList;

/** Stores a named sequence of command lines. */
public final class Macro {

    private final String name;
    private final ArrayList<String> commandLines;

    /**
     * Creates an empty named macro.
     *
     * @param name the macro name
     * @throws IllegalArgumentException if the name is null or blank
     */
    public Macro(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Macro name cannot be empty.");
        }

        this.name = name;
        commandLines = new ArrayList<String>();
    }

    /**
     * Returns the macro name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Adds a command to the macro.
     *
     * @param command the complete command line
     * @throws IllegalArgumentException if the command is null or blank
     */
    public void addCommand(String command) {
        if (command == null || command.isBlank()) {

            throw new IllegalArgumentException("Command cannot be empty.");

        }

        commandLines.add(command);
    }

    /**
     * Returns a copy of the recorded commands.
     *
     * @return commands in their recorded order
     */
    public ArrayList<String> getCommands() {

        return new ArrayList<String>(commandLines);

    }
}