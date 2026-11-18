package edu.arizona.documenteditor.statistics;

import edu.arizona.documenteditor.command.InsertTextCommand;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void countsTitlesAndParagraphText() {
        Document document = new Document();
        Section section = new Section("Intro");
        section.addChild(new Paragraph("Hello world"));
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        // "Intro" has 5 characters; "Hello world" has 11.
        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }

    @Test
    void countsNestedSectionsAndEmptyParagraphs() {
        Document document = new Document();

        Section section = new Section("Main");
        section.addChild(new Paragraph("One two"));

        Section nested = new Section("Sub");
        nested.addChild(new Paragraph("Three"));
        nested.addChild(new Paragraph(""));

        section.addChild(nested);
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        assertEquals(5, statistics.getWordCount());
        assertEquals(19, statistics.getCharacterCount());
        assertEquals(3, statistics.getParagraphCount());
    }

    @Test
    void handlesExtraWhitespace() {
        Document document = new Document();
        Section section = new Section("Intro");
        section.addChild(new Paragraph("  Hello\tworld  "));
        document.addSection(section);

        DocumentStatistics statistics = new DocumentStatistics(document);

        // Stored spaces and the tab count as characters.
        assertEquals(3, statistics.getWordCount());
        assertEquals(20, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }

    @Test
    void updatesAfterEditUndoAndRedo() {
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

        InsertTextCommand command =
                new InsertTextCommand(paragraph, 5, " world");

        engine.execute(command);

        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        engine.undo();

        assertEquals(2, statistics.getWordCount());
        assertEquals(10, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());

        engine.redo();

        assertEquals(3, statistics.getWordCount());
        assertEquals(16, statistics.getCharacterCount());
        assertEquals(1, statistics.getParagraphCount());
    }
}