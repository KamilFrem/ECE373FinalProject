package edu.arizona.documenteditor.cli;

import java.util.ArrayList;
import java.util.Locale;

/** Separates complete command lines while preserving quoted arguments. */
public final class CommandParser {
    /**
     * Parses a line. Double quotes preserve spaces and empty arguments.
     * Inside quotes, a backslash before a quote or backslash escapes it.
     * @param line input line
     * @return command name in lowercase and arguments with their case preserved
     * @throws NullPointerException if line is null
     * @throws IllegalArgumentException if input is empty or quotes are unclosed
     */
    public ParsedCommand parse(String line) {
        if (line == null) {
            throw new NullPointerException("Command line cannot be null.");
        }
        ArrayList<String> parts = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        boolean started = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (insideQuotes && character == '\\' && i + 1 < line.length() && (line.charAt(i + 1) == '"' || line.charAt(i + 1) == '\\')) {

                i++;
                current.append(line.charAt(i));
                started = true;

            }
            else if (character == '"') {

                insideQuotes = !insideQuotes;
                started = true;

            }
            else if (Character.isWhitespace(character) && !insideQuotes) {

                if (started) {
                    parts.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
            }
            else {
                current.append(character);
                started = true;
            }
        }
        if (insideQuotes) {
            throw new IllegalArgumentException("A closing double quote is missing.");
        }

        if (started) {
            parts.add(current.toString());
        }

        if (parts.isEmpty() || parts.get(0).isBlank()) {
            throw new IllegalArgumentException("Enter a command.");
        }

        ArrayList<String> arguments = new ArrayList<String>();
        for (int i = 1; i < parts.size(); i++) {
            arguments.add(parts.get(i));
        }
        return new ParsedCommand(parts.get(0).toLowerCase(Locale.ROOT), arguments);
    }
}
