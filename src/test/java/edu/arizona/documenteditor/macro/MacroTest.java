package edu.arizona.documenteditor.macro;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MacroTest {

    @Test
    void storesNameAndCommandsInOrder() {
        Macro macro = new Macro("formatting");

        macro.addCommand("first command");
        macro.addCommand("second command");

        assertEquals("formatting", macro.getName());
        assertEquals(2, macro.getCommands().size());
        assertEquals("first command", macro.getCommands().get(0));
        assertEquals("second command", macro.getCommands().get(1));
    }

    @Test
    void rejectsInvalidNames() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Macro(null));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Macro(""));

        assertThrows(
                IllegalArgumentException.class,
                () -> new Macro("   "));
    }

    @Test
    void rejectsInvalidCommandsWithoutChangingTheMacro() {
        Macro macro = new Macro("example");
        macro.addCommand("valid command");

        assertThrows(
                IllegalArgumentException.class,
                () -> macro.addCommand(null));

        assertThrows(
                IllegalArgumentException.class,
                () -> macro.addCommand(""));

        assertThrows(
                IllegalArgumentException.class,
                () -> macro.addCommand("   "));

        assertEquals(1, macro.getCommands().size());
        assertEquals("valid command", macro.getCommands().get(0));
    }

    @Test
    void returnedCommandsCannotChangeTheMacro() {
        Macro macro = new Macro("example");
        macro.addCommand("original command");

        ArrayList<String> commands = macro.getCommands();
        commands.clear();
        commands.add("different command");

        assertEquals(1, macro.getCommands().size());
        assertEquals("original command", macro.getCommands().get(0));
    }
}