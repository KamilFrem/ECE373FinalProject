package edu.arizona.documenteditor.cli;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private final CommandParser parser = new CommandParser();

    @Test
    void parsesACommandWithoutArguments() {
        ParsedCommand command = parser.parse("undo");
        assertEquals("undo", command.getName());
        assertEquals(0, command.getArguments().size());
    }

    @Test
    void keepsQuotedTextTogether() {
        ParsedCommand command = parser.parse("insert 1 0 \"Hello world\"");
        assertEquals("insert", command.getName());
        assertEquals(3, command.getArguments().size());
        assertEquals("Hello world", command.getArguments().get(2));
    }

    @Test
    void handlesExtraSpacesAndTabs() {
        ParsedCommand command = parser.parse("  insert\t 1   0  \"Hello\"  ");
        assertEquals("insert", command.getName());
        assertEquals(3, command.getArguments().size());
        assertEquals("Hello", command.getArguments().get(2));
    }

    @Test
    void changesOnlyTheCommandNameToLowercase() {
        ParsedCommand command = parser.parse("INSERT 1 0 \"Hello WORLD\"");
        assertEquals("insert", command.getName());
        assertEquals("Hello WORLD", command.getArguments().get(2));
    }

    @Test
    void preservesAnEmptyQuotedArgument() {
        ParsedCommand command = parser.parse("replacefirst \"cat\" \"\"");
        assertEquals(2, command.getArguments().size());
        assertEquals("", command.getArguments().get(1));
    }

    @Test
    void preservesSpacesInsideQuotes() {
        assertEquals("  Hello  world  ", parser.parse(
                "insert 1 0 \"  Hello  world  \"").getArguments().get(2));
    }

    @Test
    void rejectsAnUnclosedQuote() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("insert 1 0 \"Hello"));
    }

    @Test
    void rejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("   \t  "));
    }

    @Test
    void rejectsAnEmptyCommandName() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("\"\""));
    }

    @Test
    void rejectsNullInput() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    void returnedArgumentsCannotChangeTheStoredCommand() {
        ParsedCommand command = parser.parse("insert 1 0 \"Hello\"");
        ArrayList<String> arguments = command.getArguments();
        arguments.clear();
        assertEquals(3, command.getArguments().size());
    }

    @Test
    void supportsQuotedQuotesAndWindowsPaths() {
        String line = "paragraph 1 \"He said \\\"Hi\\\"\"";
        assertEquals("He said \"Hi\"", parser.parse(line).getArguments().get(1));
        assertEquals("C:\\Users\\Super\\demo.txt", parser.parse(
                "script \"C:\\Users\\Super\\demo.txt\"").getArguments().get(0));
    }
}
