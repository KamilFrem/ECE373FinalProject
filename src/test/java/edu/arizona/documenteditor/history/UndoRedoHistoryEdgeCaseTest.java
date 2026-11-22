package edu.arizona.documenteditor.history;

import edu.arizona.documenteditor.command.InsertTextCommand;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UndoRedoHistoryEdgeCaseTest {

    private final TextStyle regular =
            new TextStyle(false, false, "Arial", 12, "black");

    @Test
    void newEditAfterUndoClearsRedoHistory() {
        Paragraph paragraph = new Paragraph("");
        HistoryManager history = new HistoryManager();

        InsertTextCommand firstCommand =
                new InsertTextCommand(paragraph, 0, "Hello", regular);

        history.executeCommand(firstCommand);

        assertEquals("Hello", paragraph.getText());
        assertEquals(1, history.undoCount());

        history.undo();

        assertEquals("", paragraph.getText());
        assertEquals(1, history.redoCount());

        InsertTextCommand secondCommand =
                new InsertTextCommand(paragraph, 0, "World", regular);

        history.executeCommand(secondCommand);

        assertEquals("World", paragraph.getText());
        assertEquals(1, history.undoCount());
        assertEquals(0, history.redoCount());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());

        assertThrows(
                IllegalStateException.class,
                history::redo
        );

        assertEquals("World", paragraph.getText());
    }

    @Test
    void multipleUndoAndRedoOperationsPreserveCommandOrder() {
        Paragraph paragraph = new Paragraph("");
        HistoryManager history = new HistoryManager();

        history.executeCommand(
                new InsertTextCommand(paragraph, 0, "A", regular)
        );

        history.executeCommand(
                new InsertTextCommand(paragraph, 1, "B", regular)
        );

        history.executeCommand(
                new InsertTextCommand(paragraph, 2, "C", regular)
        );

        assertEquals("ABC", paragraph.getText());

        history.undo();
        assertEquals("AB", paragraph.getText());

        history.undo();
        assertEquals("A", paragraph.getText());

        assertEquals(1, history.undoCount());
        assertEquals(2, history.redoCount());

        history.redo();
        assertEquals("AB", paragraph.getText());

        history.redo();
        assertEquals("ABC", paragraph.getText());

        assertEquals(3, history.undoCount());
        assertEquals(0, history.redoCount());
    }

    @Test
    void undoAndRedoAtEmptyHistoryBoundariesAreRejected() {
        HistoryManager history = new HistoryManager();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());

        assertEquals(0, history.undoCount());
        assertEquals(0, history.redoCount());

        assertThrows(
                IllegalStateException.class,
                history::undo
        );

        assertThrows(
                IllegalStateException.class,
                history::redo
        );

        assertEquals(0, history.undoCount());
        assertEquals(0, history.redoCount());
    }

    @Test
    void failedNewCommandDoesNotChangeDocumentOrHistory() {
        Paragraph paragraph = new Paragraph("");
        HistoryManager history = new HistoryManager();

        history.executeCommand(
                new InsertTextCommand(paragraph, 0, "Hello", regular)
        );

        history.undo();

        assertEquals("", paragraph.getText());
        assertEquals(0, history.undoCount());
        assertEquals(1, history.redoCount());

        InsertTextCommand invalidCommand =
                new InsertTextCommand(paragraph, 20, "Invalid", regular);

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> history.executeCommand(invalidCommand)
        );

        assertEquals("", paragraph.getText());

        assertEquals(0, history.undoCount());
        assertEquals(1, history.redoCount());

        assertFalse(history.canUndo());
        assertTrue(history.canRedo());

        // The previous valid command should still be redoable.
        history.redo();

        assertEquals("Hello", paragraph.getText());
        assertEquals(1, history.undoCount());
        assertEquals(0, history.redoCount());
    }

    @Test
    void clearingHistoryDoesNotModifyDocumentContent() {
        Paragraph paragraph = new Paragraph("");
        HistoryManager history = new HistoryManager();

        history.executeCommand(
                new InsertTextCommand(paragraph, 0, "Hello", regular)
        );

        history.undo();

        assertEquals("", paragraph.getText());
        assertEquals(1, history.redoCount());

        history.clear();

        assertEquals("", paragraph.getText());
        assertEquals(0, history.undoCount());
        assertEquals(0, history.redoCount());

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }
}
