package edu.arizona.documenteditor.history;

import edu.arizona.documenteditor.command.EditCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager history;

    @BeforeEach
    void setUp() {
        history = new HistoryManager();
    }

    private static class TestCommand implements EditCommand {

        private boolean executed;
        private boolean failExecute;
        private boolean failUndo;

        @Override
        public void execute() {
            if (failExecute) {
                throw new IllegalStateException("Execution failed.");
            }

            executed = true;
        }

        @Override
        public void undo() {
            if (failUndo) {
                throw new IllegalStateException("Undo failed.");
            }

            executed = false;
        }
    }

    @Test
    void executesAndRecordsCommand() {
        TestCommand command = new TestCommand();

        history.executeCommand(command);

        assertTrue(command.executed);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(1, history.undoCount());
    }

    @Test
    void undoesCommand() {
        TestCommand command = new TestCommand();

        history.executeCommand(command);
        history.undo();

        assertFalse(command.executed);
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
        assertEquals(1, history.redoCount());
    }

    @Test
    void redoesCommand() {
        TestCommand command = new TestCommand();

        history.executeCommand(command);
        history.undo();
        history.redo();

        assertTrue(command.executed);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void successfulNewCommandClearsRedoHistory() {
        TestCommand first = new TestCommand();
        TestCommand second = new TestCommand();

        history.executeCommand(first);
        history.undo();

        assertTrue(history.canRedo());

        history.executeCommand(second);

        assertFalse(history.canRedo());
        assertEquals(1, history.undoCount());
    }

    @Test
    void failedExecutionDoesNotEnterHistory() {
        TestCommand command = new TestCommand();
        command.failExecute = true;

        assertThrows(
                IllegalStateException.class,
                () -> history.executeCommand(command)
        );

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(0, history.undoCount());
    }

    @Test
    void failedUndoDoesNotChangeHistory() {
        TestCommand command = new TestCommand();

        history.executeCommand(command);
        command.failUndo = true;

        assertThrows(
                IllegalStateException.class,
                () -> history.undo()
        );

        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
        assertEquals(1, history.undoCount());
    }

    @Test
    void failedRedoDoesNotChangeHistory() {
        TestCommand command = new TestCommand();

        history.executeCommand(command);
        history.undo();

        command.failExecute = true;

        assertThrows(
                IllegalStateException.class,
                () -> history.redo()
        );

        assertFalse(history.canUndo());
        assertTrue(history.canRedo());
        assertEquals(1, history.redoCount());
    }
}
