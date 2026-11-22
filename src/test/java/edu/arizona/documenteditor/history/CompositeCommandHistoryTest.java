package edu.arizona.documenteditor.history;

import edu.arizona.documenteditor.command.CompositeCommand;
import edu.arizona.documenteditor.command.InsertTextCommand;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompositeCommandHistoryTest {

    private final TextStyle regular =
            new TextStyle(false, false, "Arial", 12, "black");

    @Test
    void compositeCommandIsOneUndoableHistoryEntry() {
        Paragraph paragraph = new Paragraph("");
        HistoryManager history = new HistoryManager();

        CompositeCommand composite = new CompositeCommand(
                new InsertTextCommand(
                        paragraph, 0, "Hello", regular),
                new InsertTextCommand(
                        paragraph, 5, " world", regular)
        );

        history.executeCommand(composite);

        assertEquals("Hello world", paragraph.getText());

        // Both insertions should count as one history entry.
        assertEquals(1, history.undoCount());
        assertEquals(0, history.redoCount());

        // One undo should reverse both insertions.
        history.undo();

        assertEquals("", paragraph.getText());
        assertEquals(0, history.undoCount());
        assertEquals(1, history.redoCount());

        // One redo should restore both insertions.
        history.redo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(1, history.undoCount());
        assertEquals(0, history.redoCount());
    }

    @Test
    void failedCompositeCommandPreservesExistingRedoHistory() {
        Paragraph paragraph = new Paragraph("");
        HistoryManager history = new HistoryManager();

        // Create a valid command and move it into redo history.
        history.executeCommand(
                new InsertTextCommand(
                        paragraph, 0, "Original", regular)
        );

        history.undo();

        assertEquals("", paragraph.getText());
        assertEquals(1, history.redoCount());

        // The first command succeeds, but the second is invalid.
        CompositeCommand failingComposite = new CompositeCommand(
                new InsertTextCommand(
                        paragraph, 0, "Partial", regular),
                new InsertTextCommand(
                        paragraph, 99, "Invalid", regular)
        );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> history.executeCommand(failingComposite)
        );

        // The successful first operation must have been rolled back.
        assertEquals("", paragraph.getText());

        // The failed composite must not alter either history stack.
        assertEquals(0, history.undoCount());
        assertEquals(1, history.redoCount());

        assertFalse(history.canUndo());
        assertTrue(history.canRedo());

        // The original command should still be redoable.
        history.redo();

        assertEquals("Original", paragraph.getText());
        assertEquals(1, history.undoCount());
        assertEquals(0, history.redoCount());
    }
}