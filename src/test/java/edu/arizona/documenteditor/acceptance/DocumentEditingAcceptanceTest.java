package edu.arizona.documenteditor.acceptance;

import edu.arizona.documenteditor.command.*;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentEditingAcceptanceTest {

    @Test
    void completeEditingUndoAndRedoWorkflow() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);

        // Create a section.
        CreateSectionCommand createSection =
                new CreateSectionCommand(document, 0, "Introduction");

        engine.execute(createSection);

        Section section = createSection.getSection();

        // Create a paragraph.
        CreateParagraphCommand createParagraph =
                new CreateParagraphCommand(section, 0, "Hello world");

        engine.execute(createParagraph);

        Paragraph paragraph = createParagraph.getParagraph();

        // Insert text.
        engine.execute(
                new InsertTextCommand(paragraph, 11, "!"));

        assertEquals("Hello world!", paragraph.getText());

        // Replace text.
        engine.execute(
                new ReplaceTextCommand(paragraph, 6, 11, "Java"));

        assertEquals("Hello Java!", paragraph.getText());

        // Delete text.
        engine.execute(
                new DeleteTextCommand(paragraph, 10, 11));

        assertEquals("Hello Java", paragraph.getText());

        // Undo the deletion.
        engine.undo();
        assertEquals("Hello Java!", paragraph.getText());

        // Undo the replacement.
        engine.undo();
        assertEquals("Hello world!", paragraph.getText());

        // Undo the insertion.
        engine.undo();
        assertEquals("Hello world", paragraph.getText());

        // Redo the insertion.
        engine.redo();
        assertEquals("Hello world!", paragraph.getText());

        // Redo the replacement.
        engine.redo();
        assertEquals("Hello Java!", paragraph.getText());

        // Redo the deletion.
        engine.redo();
        assertEquals("Hello Java", paragraph.getText());

        assertTrue(engine.canUndo());
        assertFalse(engine.canRedo());
    }

    @Test
    void failedEditPreservesDocumentAndHistory() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);

        CreateSectionCommand createSection =
                new CreateSectionCommand(document, 0, "Introduction");

        engine.execute(createSection);

        Paragraph paragraph = new Paragraph("Original");
        createSection.getSection().addChild(paragraph);

        String originalText = document.getText();
        int originalUndoCount = engine.getHistoryManager().undoCount();

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> engine.execute(
                        new InsertTextCommand(paragraph, 100, "Invalid"))
        );

        assertEquals(originalText, document.getText());
        assertEquals(
                originalUndoCount,
                engine.getHistoryManager().undoCount()
        );
    }

    @Test
    void newEditClearsRedoHistory() {
        DocumentEngine engine = new DocumentEngine();
        Document document = engine.getDocument();

        engine.execute(
                new CreateSectionCommand(document, 0, "First"));

        engine.undo();

        assertTrue(engine.canRedo());

        engine.execute(
                new CreateSectionCommand(document, 0, "Second"));

        assertFalse(engine.canRedo());
        assertEquals(1, document.getSections().size());
        assertEquals("Second", document.getSections().get(0).getText());
    }

    @Test
    void splitAndMergeCanBeUndoneAndRedone() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);

        Section section = new Section("Introduction");
        Paragraph paragraph = new Paragraph("Hello world");

        section.addChild(paragraph);
        document.addSection(section);

        SplitParagraphCommand split =
                new SplitParagraphCommand(paragraph, 6);

        engine.execute(split);

        assertEquals(2, section.getChildren().size());
        assertEquals("Hello ", paragraph.getText());
        assertEquals("world", split.getSecondParagraph().getText());

        engine.undo();

        assertEquals(1, section.getChildren().size());
        assertEquals("Hello world", paragraph.getText());

        engine.redo();

        assertEquals(2, section.getChildren().size());

        MergeParagraphsCommand merge =
                new MergeParagraphsCommand(
                        section, paragraph, split.getSecondParagraph());

        engine.execute(merge);

        assertEquals(1, section.getChildren().size());
        assertEquals("Hello world", paragraph.getText());

        engine.undo();

        assertEquals(2, section.getChildren().size());
    }
}
