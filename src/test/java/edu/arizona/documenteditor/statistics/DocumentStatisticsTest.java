package edu.arizona.documenteditor.statistics;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.checkpoint.RestoreCheckpointCommand;
import edu.arizona.documenteditor.command.InsertTextCommand;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentStatisticsTest {

    @Test
    void countsAnEmptyDocument() {
        Document document = new Document();
        DocumentStatistics statistics = new DocumentStatistics(document);

        assertEquals(0, statistics.getWordCount());
        assertEquals(0, statistics.getCharacterCount());
        assertEquals(0, statistics.getParagraphCount());
    }

    @Test
    void countsSectionTitleAndParagraph() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("Hello world");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }

    @Test
    void countsNestedSectionsAndEmptyParagraphs() {
        Document document = new Document();

        Section mainSection = new Section("Main");
        mainSection.addChild(new Paragraph("One two"));

        Section nestedSection = new Section("Sub");
        nestedSection.addChild(new Paragraph("Three"));
        nestedSection.addChild(new Paragraph(""));

        mainSection.addChild(nestedSection);
        document.addSection(mainSection);

        DocumentStatistics statistics = new DocumentStatistics(document);

        assertEquals(5, statistics.getWordCount());
        assertEquals(19, statistics.getCharacterCount());
        assertEquals(3, statistics.getParagraphCount());
    }

    @Test
    void countsWhitespaceAsCharactersButNotWords() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph("  Hello\tworld  "));
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        assertEquals(3, statistics.getWordCount());
        assertEquals(20, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }

    @Test
    void countsAWhitespaceOnlyParagraph() {
        Document document = new Document();
        Section section = new Section("");

        section.addChild(new Paragraph(" \t\n "));
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        assertEquals(0, statistics.getWordCount());
        assertEquals(4, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }

    @Test
    void rejectsANullDocument() {
        assertThrows(
                NullPointerException.class,
                () -> new DocumentStatistics(null));
    }

    @Test
    void recalculatesInsteadOfAddingToPreviousCounts() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph("Hello"));
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        statistics.onDocumentChanged(document);
        statistics.onDocumentChanged(document);

        assertEquals(2, statistics.getWordCount());
        assertEquals(10, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        statistics.onDocumentChanged(new Document());

        assertEquals(0, statistics.getWordCount());
        assertEquals(0, statistics.getCharacterCount());
        assertEquals(0, statistics.getParagraphCount());
    }

    @Test
    void updatesAfterEditingUndoAndRedo() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("Hello");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);
        DocumentStatistics statistics = new DocumentStatistics(document);

        engine.addChangeListener(statistics);

        assertEquals(2, statistics.getWordCount());
        assertEquals(10, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        InsertTextCommand insert =
                new InsertTextCommand(paragraph, 5, " world");

        engine.execute(insert);

        assertEquals("Hello world", paragraph.getText());
        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        engine.undo();

        assertEquals("Hello", paragraph.getText());
        assertEquals(2, statistics.getWordCount());
        assertEquals(10, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        engine.redo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }

    @Test
    void updatesAfterCheckpointRestoreUndoAndRedo() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("Hello");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);
        DocumentStatistics statistics = new DocumentStatistics(document);

        engine.addChangeListener(statistics);

        CheckpointManager checkpoints = new CheckpointManager();
        checkpoints.createCheckpoint("original", document);

        InsertTextCommand insert =
                new InsertTextCommand(paragraph, 5, " world");

        engine.execute(insert);

        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        RestoreCheckpointCommand restore =
                new RestoreCheckpointCommand(
                        document, checkpoints, "original");

        engine.execute(restore);

        assertEquals("Hello", document.getParagraphs().get(0).getText());
        assertEquals(2, statistics.getWordCount());
        assertEquals(10, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        engine.undo();

        assertEquals(
                "Hello world",
                document.getParagraphs().get(0).getText());

        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        engine.redo();

        assertEquals("Hello", document.getParagraphs().get(0).getText());
        assertEquals(2, statistics.getWordCount());
        assertEquals(10, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }
}