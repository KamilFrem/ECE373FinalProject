package edu.arizona.documenteditor.macro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MacroManagerTest {

    @Test
    void startsWithoutAnActiveRecording() {
        MacroManager manager = new MacroManager();

        assertFalse(manager.isRecording());
    }

    @Test
    void recordsAndSavesCommandsInOrder() {
        MacroManager manager = new MacroManager();

        manager.startRecording("example");

        assertTrue(manager.isRecording());

        manager.record("first command");
        manager.record("second command");
        manager.stopRecording();

        assertFalse(manager.isRecording());

        Macro macro = manager.getMacro("example");

        assertEquals("example", macro.getName());
        assertEquals(2, macro.getCommands().size());
        assertEquals("first command", macro.getCommands().get(0));
        assertEquals("second command", macro.getCommands().get(1));
    }

    @Test
    void rejectsRecordingCommandsWhenRecordingIsInactive() {
        MacroManager manager = new MacroManager();

        assertThrows(
                IllegalStateException.class,
                () -> manager.record("some command"));

        assertThrows(
                IllegalStateException.class,
                () -> manager.stopRecording());

        assertFalse(manager.isRecording());
    }

    @Test
    void rejectsASecondRecordingWithoutLosingTheFirst() {
        MacroManager manager = new MacroManager();

        manager.startRecording("first");
        manager.record("original command");

        assertThrows(
                IllegalStateException.class,
                () -> manager.startRecording("second"));

        assertTrue(manager.isRecording());

        manager.record("another command");
        manager.stopRecording();

        Macro macro = manager.getMacro("first");

        assertEquals(2, macro.getCommands().size());
        assertEquals("original command", macro.getCommands().get(0));
        assertEquals("another command", macro.getCommands().get(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.getMacro("second"));
    }

    @Test
    void rejectsDuplicateNamesWithoutReplacingSavedCommands() {
        MacroManager manager = new MacroManager();

        manager.startRecording("example");
        manager.record("original command");
        manager.stopRecording();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.startRecording("example"));

        assertFalse(manager.isRecording());

        Macro macro = manager.getMacro("example");

        assertEquals(1, macro.getCommands().size());
        assertEquals("original command", macro.getCommands().get(0));
    }

    @Test
    void rejectsInvalidNamesWithoutStartingRecording() {
        MacroManager manager = new MacroManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.startRecording(null));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.startRecording(""));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.startRecording("   "));

        assertFalse(manager.isRecording());
    }

    @Test
    void rejectsMissingAndInvalidMacroNames() {
        MacroManager manager = new MacroManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.getMacro("missing"));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.getMacro(null));

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.getMacro("   "));
    }

    @Test
    void keepsSeparateRecordingsIndependent() {
        MacroManager manager = new MacroManager();

        manager.startRecording("first");
        manager.record("first command");
        manager.stopRecording();

        manager.startRecording("second");
        manager.record("second command");
        manager.stopRecording();

        Macro first = manager.getMacro("first");
        Macro second = manager.getMacro("second");

        assertEquals(1, first.getCommands().size());
        assertEquals("first command", first.getCommands().get(0));

        assertEquals(1, second.getCommands().size());
        assertEquals("second command", second.getCommands().get(0));
    }

    @Test
    void changingRetrievedMacroDoesNotChangeSavedMacro() {
        MacroManager manager = new MacroManager();

        manager.startRecording("example");
        manager.record("original command");
        manager.stopRecording();

        Macro retrieved = manager.getMacro("example");
        retrieved.addCommand("extra command");

        Macro saved = manager.getMacro("example");

        assertEquals(1, saved.getCommands().size());
        assertEquals("original command", saved.getCommands().get(0));
    }

    @Test
    void invalidCommandDoesNotEndOrDamageRecording() {
        MacroManager manager = new MacroManager();

        manager.startRecording("example");
        manager.record("first command");

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.record("   "));

        assertTrue(manager.isRecording());

        manager.record("second command");
        manager.stopRecording();

        Macro macro = manager.getMacro("example");

        assertEquals(2, macro.getCommands().size());
        assertEquals("first command", macro.getCommands().get(0));
        assertEquals("second command", macro.getCommands().get(1));
    }
}