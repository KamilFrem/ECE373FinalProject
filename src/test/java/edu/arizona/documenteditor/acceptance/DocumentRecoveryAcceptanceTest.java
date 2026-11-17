package edu.arizona.documenteditor.acceptance;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.checkpoint.RestoreCheckpointCommand;
import edu.arizona.documenteditor.command.CreateSectionCommand;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentRecoveryAcceptanceTest {

    @Test
    void completeCheckpointRecoveryWorkflow() {
        TextStyle regular =
                new TextStyle(false, false, "Arial", 12, "black");

        TextStyle bold =
                new TextStyle(true, false, "Arial", 12, "black");

        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();

        // Build a document with nested structure and formatting.
        Section introduction = new Section("Introduction");

        Section details = new Section("Details");

        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Important ", regular),
                new TextRun("original text", bold)
        ));

        details.addChild(paragraph);
        introduction.addChild(details);
        document.addSection(introduction);

        // Save the initial state.
        checkpoints.createCheckpoint("initial", document);

        String originalText = document.getText();

        // Make changes after the checkpoint.
        paragraph.setRuns(List.of(
                new TextRun("Modified document", regular)
        ));

        introduction.addChild(new Paragraph("New paragraph"));

        assertNotEquals(originalText, document.getText());

        // Restore the initial checkpoint through the engine.
        RestoreCheckpointCommand restore =
                new RestoreCheckpointCommand(
                        document, checkpoints, "initial");

        engine.execute(restore);

        assertEquals(originalText, document.getText());
        assertEquals(1, document.getSections().size());

        Section restoredIntroduction = document.getSections().get(0);
        Section restoredDetails =
                (Section) restoredIntroduction.getChildren().get(0);

        Paragraph restoredParagraph =
                (Paragraph) restoredDetails.getChildren().get(0);

        assertEquals(2, restoredParagraph.getRuns().size());
        assertEquals(regular, restoredParagraph.getRuns().get(0).getStyle());
        assertEquals(bold, restoredParagraph.getRuns().get(1).getStyle());

        // Undo the restoration to recover the modified document.
        engine.undo();

        assertNotEquals(originalText, document.getText());
        assertTrue(document.getText().contains("Modified document"));

        // Redo the restoration.
        engine.redo();

        assertEquals(originalText, document.getText());
    }

    @Test
    void checkpointSaveDoesNotClearRedoHistory() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();

        engine.execute(
                new CreateSectionCommand(document, 0, "Introduction"));

        engine.undo();

        assertTrue(engine.canRedo());

        checkpoints.createCheckpoint("empty", document);

        assertTrue(engine.canRedo());
        assertEquals(0, document.getSections().size());
    }

    @Test
    void missingCheckpointDoesNotChangeDocument() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();

        engine.execute(
                new CreateSectionCommand(document, 0, "Introduction"));

        String originalText = document.getText();
        int originalUndoCount = engine.getHistoryManager().undoCount();

        assertThrows(
                IllegalArgumentException.class,
                () -> new RestoreCheckpointCommand(
                        document, checkpoints, "missing")
        );

        assertEquals(originalText, document.getText());
        assertEquals(
                originalUndoCount,
                engine.getHistoryManager().undoCount()
        );
    }
}