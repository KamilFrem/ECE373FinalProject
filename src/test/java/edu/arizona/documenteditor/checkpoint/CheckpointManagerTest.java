package edu.arizona.documenteditor.checkpoint;

import edu.arizona.documenteditor.history.HistoryManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointManagerTest {

    private final TextStyle regular =
            new TextStyle(false, false, "Arial", 12, "black");

    private final TextStyle bold =
            new TextStyle(true, false, "Arial", 12, "black");

    private Document createDocument() {
        Document document = new Document();

        Section section = new Section("Introduction");
        section.addChild(new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("world", bold)
        )));

        document.addSection(section);

        return document;
    }

    @Test
    void createsAndRetrievesCheckpoint() {
        Document document = createDocument();
        CheckpointManager manager = new CheckpointManager();

        manager.createCheckpoint("initial", document);

        assertTrue(manager.hasCheckpoint("initial"));
        assertEquals(1, manager.checkpointCount());

        Document restored =
                manager.getCheckpoint("initial").getDocument();

        assertEquals("Hello world", restored.getSections()
                .get(0).getChildren().get(0).getText());
    }

    @Test
    void checkpointPreservesFormatting() {
        Document document = createDocument();
        CheckpointManager manager = new CheckpointManager();

        manager.createCheckpoint("formatted", document);

        Paragraph restoredParagraph = (Paragraph) manager
                .getCheckpoint("formatted")
                .getDocument()
                .getSections().get(0).getChildren().get(0);

        assertEquals(2, restoredParagraph.getRuns().size());
        assertEquals(regular, restoredParagraph.getRuns().get(0).getStyle());
        assertEquals(bold, restoredParagraph.getRuns().get(1).getStyle());
    }

    @Test
    void snapshotIsIndependentOfOriginalDocument() {
        Document document = createDocument();
        DocumentSnapshot snapshot = new DocumentSnapshot(document);

        Paragraph original = (Paragraph) document.getSections()
                .get(0).getChildren().get(0);

        original.setRuns(List.of(new TextRun("Changed", regular)));

        Document restored = snapshot.getDocument();

        assertEquals(
                "Hello world",
                restored.getSections().get(0)
                        .getChildren().get(0).getText()
        );
    }

    @Test
    void returnedSnapshotCopyCannotModifyStoredCheckpoint() {
        Document document = createDocument();
        CheckpointManager manager = new CheckpointManager();

        manager.createCheckpoint("initial", document);

        Document copy =
                manager.getCheckpoint("initial").getDocument();

        Paragraph paragraph = (Paragraph) copy.getSections()
                .get(0).getChildren().get(0);

        paragraph.setRuns(List.of(new TextRun("Changed", regular)));

        Document secondCopy =
                manager.getCheckpoint("initial").getDocument();

        assertEquals(
                "Hello world",
                secondCopy.getSections().get(0)
                        .getChildren().get(0).getText()
        );
    }

    @Test
    void restoreCheckpointCanBeUndoneAndRedone() {
        Document document = createDocument();
        CheckpointManager manager = new CheckpointManager();
        HistoryManager history = new HistoryManager();

        manager.createCheckpoint("initial", document);

        Paragraph paragraph = (Paragraph) document.getSections()
                .get(0).getChildren().get(0);

        paragraph.setRuns(List.of(new TextRun("Changed", regular)));

        RestoreCheckpointCommand restore =
                new RestoreCheckpointCommand(
                        document, manager, "initial");

        history.executeCommand(restore);

        assertEquals(
                "Hello world",
                document.getSections().get(0)
                        .getChildren().get(0).getText()
        );

        history.undo();

        assertEquals(
                "Changed",
                document.getSections().get(0)
                        .getChildren().get(0).getText()
        );

        history.redo();

        assertEquals(
                "Hello world",
                document.getSections().get(0)
                        .getChildren().get(0).getText()
        );
    }

    @Test
    void savingCheckpointDoesNotChangeDocumentOrHistory() {
        Document document = createDocument();
        CheckpointManager manager = new CheckpointManager();
        HistoryManager history = new HistoryManager();

        history.executeCommand(
                new edu.arizona.documenteditor.command.CreateSectionCommand(
                        document, 1, "Conclusion"));

        int undoCount = history.undoCount();
        String originalText = document.getText();

        manager.createCheckpoint("saved", document);

        assertEquals(originalText, document.getText());
        assertEquals(undoCount, history.undoCount());
        assertEquals(0, history.redoCount());
    }

    @Test
    void rejectsUnknownCheckpoint() {
        CheckpointManager manager = new CheckpointManager();

        assertThrows(
                IllegalArgumentException.class,
                () -> manager.getCheckpoint("missing")
        );
    }

    @Test
    void removesCheckpoint() {
        CheckpointManager manager = new CheckpointManager();

        manager.createCheckpoint("initial", createDocument());

        assertTrue(manager.removeCheckpoint("initial"));
        assertFalse(manager.hasCheckpoint("initial"));
        assertFalse(manager.removeCheckpoint("initial"));
    }
}
