package edu.arizona.documenteditor.acceptance;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.checkpoint.DocumentSnapshot;
import edu.arizona.documenteditor.checkpoint.RestoreCheckpointCommand;
import edu.arizona.documenteditor.command.CompositeCommand;
import edu.arizona.documenteditor.command.CreateParagraphCommand;
import edu.arizona.documenteditor.command.CreateSectionCommand;
import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.command.MoveTextCommand;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-level tests for editing, history, engine, and recovery.
 * These tests exercise complete workflows instead of isolated methods.
 */
class EditingRecoveryIntegrationTest {

    @Test
    void movingStyledTextBetweenParagraphsCanBeUndoneAndRedone() {
        TextStyle regular = new TextStyle(false, false, "Arial", 12, "black");
        TextStyle bold = new TextStyle(true, false, "Arial", 12, "black");
        TextStyle italic = new TextStyle(false, true, "Arial", 12, "blue");

        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        Section section = new Section("Movement");

        Paragraph source = new Paragraph(List.of(
                new TextRun("Keep ", regular),
                new TextRun("this", bold)));

        Paragraph destination = new Paragraph("Before ", italic);

        section.addChild(source);
        section.addChild(destination);
        document.addSection(section);

        MoveTextCommand move = new MoveTextCommand(
                source, 5, 9, destination, destination.length());

        engine.execute(move);

        assertEquals("Keep ", source.getText());
        assertEquals("Before this", destination.getText());
        assertEquals(2, destination.getRuns().size());
        assertEquals(italic, destination.getRuns().get(0).getStyle());
        assertEquals(bold, destination.getRuns().get(1).getStyle());

        engine.undo();

        assertEquals("Keep this", source.getText());
        assertEquals("Before ", destination.getText());
        assertEquals(2, source.getRuns().size());
        assertEquals(bold, source.getRuns().get(1).getStyle());

        engine.redo();

        assertEquals("Keep ", source.getText());
        assertEquals("Before this", destination.getText());
        assertEquals(bold, destination.getRuns().get(1).getStyle());
    }

    @Test
    void movingTextWithinOneParagraphCanBeUndoneAndRedone() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        Section section = new Section("Movement");
        Paragraph paragraph = new Paragraph("one two three");

        section.addChild(paragraph);
        document.addSection(section);

        engine.execute(new MoveTextCommand(
                paragraph, 0, 4, paragraph, paragraph.length()));

        assertEquals("two threeone ", paragraph.getText());

        engine.undo();

        assertEquals("one two three", paragraph.getText());

        engine.redo();

        assertEquals("two threeone ", paragraph.getText());
    }

    @Test
    void snapshotAndReturnedDocumentsCannotMutateStoredCheckpoint() {
        Document document = new Document();
        Section section = new Section("Original section");
        Paragraph paragraph = new Paragraph("Original paragraph");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentSnapshot snapshot = new DocumentSnapshot(document);

        // Changing the live document must not change the snapshot.
        paragraph.setRuns(List.of(new TextRun(
                "Changed live document",
                new TextStyle(false, false, "Arial", 12, "black"))));

        Document firstRead = snapshot.getDocument();

        assertEquals(
                "Original paragraph",
                firstRead.getParagraphs().get(0).getText());

        // Changing a returned copy must not change the stored snapshot.
        firstRead.getParagraphs().get(0).setRuns(List.of(new TextRun(
                "Changed returned copy",
                new TextStyle(false, false, "Arial", 12, "black"))));

        Document secondRead = snapshot.getDocument();

        assertEquals(
                "Original paragraph",
                secondRead.getParagraphs().get(0).getText());
    }

    @Test
    void multipleNamedCheckpointsRestoreTheirOwnIndependentStates() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();

        CreateSectionCommand create = new CreateSectionCommand(
                document, 0, "First state");

        engine.execute(create);

        checkpoints.createCheckpoint("first", document);

        Paragraph paragraph = new Paragraph("Second state");
        create.getSection().addChild(paragraph);

        checkpoints.createCheckpoint("second", document);

        paragraph.setRuns(List.of(new TextRun(
                "Third state",
                new TextStyle(false, false, "Arial", 12, "black"))));

        engine.execute(new RestoreCheckpointCommand(
                document, checkpoints, "first"));

        assertEquals("First state", document.getText());

        engine.execute(new RestoreCheckpointCommand(
                document, checkpoints, "second"));

        assertTrue(document.getText().contains("Second state"));

        engine.undo();

        assertEquals("First state", document.getText());

        engine.undo();

        assertTrue(document.getText().contains("Third state"));
    }

    @Test
    void failedCompositeCommandLeavesDocumentHistoryAndListenersUnchanged() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);

        AtomicInteger notifications = new AtomicInteger();

        engine.addChangeListener(
                ignored -> notifications.incrementAndGet());

        EditCommand valid = new CreateSectionCommand(
                document, 0, "Temporary");

        EditCommand invalid = new CreateParagraphCommand(
                new Section("Detached"),
                4,
                "Cannot be inserted at index four");

        CompositeCommand composite = new CompositeCommand(valid, invalid);

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> engine.execute(composite));

        assertTrue(document.getSections().isEmpty());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(0, engine.getHistoryManager().redoCount());
        assertEquals(0, notifications.get());
        assertFalse(engine.canUndo());
        assertFalse(engine.canRedo());
    }

    @Test
    void checkpointRestoreNotifiesListenersAndUndoRedoRemainObservable() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();

        AtomicInteger notifications = new AtomicInteger();

        engine.addChangeListener(
                ignored -> notifications.incrementAndGet());

        engine.execute(new CreateSectionCommand(
                document, 0, "Saved"));

        checkpoints.createCheckpoint("saved", document);

        // Change the document through the engine, then restore the checkpoint.
        engine.execute(new CreateSectionCommand(
                document, 1, "Later"));

        engine.execute(new RestoreCheckpointCommand(
                document, checkpoints, "saved"));

        assertEquals(1, document.getSections().size());
        assertEquals(3, notifications.get());

        engine.undo();

        assertEquals(2, document.getSections().size());
        assertEquals(4, notifications.get());

        engine.redo();

        assertEquals(1, document.getSections().size());
        assertEquals(5, notifications.get());
    }
}