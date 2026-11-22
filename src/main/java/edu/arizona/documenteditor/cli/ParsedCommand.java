package edu.arizona.documenteditor.cli;

import java.util.ArrayList;

/** Stores a command name and its ordered arguments. */
public final class ParsedCommand {
    private final String name;
    private final ArrayList<String> arguments;

    /**
     * Creates a parsed command using a copy of the arguments.
     * @param name command name
     * @param arguments ordered arguments, including empty quoted strings
     * @throws NullPointerException if a required value is null
     * @throws IllegalArgumentException if name is blank
     */
    public ParsedCommand(String name, ArrayList<String> arguments) {
        if (name == null || arguments == null) {

            throw new NullPointerException("Name and arguments cannot be null.");

        }
        if (name.isBlank()) {

            throw new IllegalArgumentException("Command name cannot be empty.");

        }
        this.name = name;
        this.arguments = new ArrayList<String>();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            if (argument == null) {
                throw new NullPointerException("An argument cannot be null.");
            }
            this.arguments.add(argument);
        }
    }

    /** @return the command name */
    public String getName() {
        return name;
    }

    /** @return an independent copy of the arguments */
    public ArrayList<String> getArguments() {
        return new ArrayList<String>(arguments);
    }
}
