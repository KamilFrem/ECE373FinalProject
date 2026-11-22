package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchTitleTest {

    private final SearchService search = new SearchService();

    @Test
    void searchesTitlesAndParagraphsInDocumentOrder() {
        Document document = new Document();
        Section section = new Section("cat title");
        Paragraph first = new Paragraph("first cat");

        Section nested = new Section("nested cat");
        Paragraph second = new Paragraph("second cat");

        section.addChild(first);
        nested.addChild(second);
        section.addChild(nested);
        document.addSection(section);

        List<SearchMatch> matches = search.find(document, "cat", MatchMode.WHOLE_WORD);

        assertEquals(4, matches.size());

        assertSame(section, matches.get(0).getTarget());
        assertEquals(0, matches.get(0).getStart());
        assertEquals(3, matches.get(0).getEnd());
        assertNull(matches.get(0).getParagraph());

        assertSame(first, matches.get(1).getTarget());
        assertSame(first, matches.get(1).getParagraph());

        assertSame(nested, matches.get(2).getTarget());
        assertSame(second, matches.get(3).getTarget());
    }

    @Test
    void supportsBothMatchingModesInTitles() {
        Document document = new Document();
        document.addSection(new Section("catalog cat"));

        List<SearchMatch> substrings = search.find(document, "cat", MatchMode.SUBSTRING);

        assertEquals(2, substrings.size());

        List<SearchMatch> words = search.find(document, "cat", MatchMode.WHOLE_WORD);

        assertEquals(1, words.size());
        assertEquals(8, words.get(0).getStart());
    }

    @Test
    void replaceFirstChangesTitleBeforeParagraph() {
        Document document = new Document();
        Section section = new Section("cat title");
        Paragraph paragraph = new Paragraph("cat paragraph");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);

        ReplaceFirstCommand command = new ReplaceFirstCommand(document, search, "cat", "elephant", MatchMode.WHOLE_WORD);

        engine.execute(command);

        assertEquals("elephant title", section.getText());
        assertEquals("cat paragraph", paragraph.getText());

        engine.undo();

        assertEquals("cat title", section.getText());
        assertEquals("cat paragraph", paragraph.getText());

        engine.redo();

        assertEquals("elephant title", section.getText());
        assertEquals("cat paragraph", paragraph.getText());
    }

    @Test
    void oneUndoRestoresAllTitlesAndParagraphs() {
        Document document = new Document();
        Section section = new Section("cat cat");
        Paragraph paragraph = new Paragraph("cat");
        Section nested = new Section("cat nested");

        section.addChild(paragraph);
        section.addChild(nested);
        document.addSection(section);

        String originalText = document.getText();
        DocumentEngine engine = new DocumentEngine(document);

        ReplaceAllCommand command = new ReplaceAllCommand(document, search, "cat", "elephant", MatchMode.WHOLE_WORD);

        engine.execute(command);

        assertEquals("elephant elephant", section.getText());
        assertEquals("elephant", paragraph.getText());
        assertEquals("elephant nested", nested.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());

        engine.undo();

        assertEquals(originalText, document.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(1, engine.getHistoryManager().redoCount());

        engine.redo();

        assertEquals("elephant elephant", section.getText());
        assertEquals("elephant", paragraph.getText());
        assertEquals("elephant nested", nested.getText());

        engine.undo();

        assertEquals(originalText, document.getText());
    }

    @Test
    void preservesTitleFormattingAndRestoresOriginalRuns() {
        TextStyle regular = new TextStyle(false, false, "Arial", 12, "black");

        TextStyle bold = new TextStyle(true, false, "Arial", 12, "black");

        List<TextRun> original = new ArrayList<TextRun>();
        original.add(new TextRun("c", bold));
        original.add(new TextRun("at", regular));
        original.add(new TextRun(" cat", bold));

        Section section = new Section(original);
        Document document = new Document();
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(document, search, "cat", "fox", MatchMode.WHOLE_WORD);

        command.execute();

        List<TextRun> expected = new ArrayList<TextRun>();
        expected.add(new TextRun("fox fox", bold));

        assertEquals("fox fox", section.getText());
        assertEquals(expected, section.getRuns());

        command.undo();

        assertEquals("cat cat", section.getText());
        assertEquals(original, section.getRuns());

        command.execute();

        assertEquals(expected, section.getRuns());
    }

    @Test
    void canDeleteAnEntireTitleAndUndoIt() {
        Document document = new Document();
        Section section = new Section("cat");
        Paragraph paragraph = new Paragraph("keep");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(document, search, "cat", "", MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals("", section.getText());
        assertEquals("keep", paragraph.getText());
        assertSame(paragraph, section.getChildren().get(0));

        command.undo();

        assertEquals("cat", section.getText());

        command.execute();

        assertEquals("", section.getText());
    }

    @Test
    void rejectsInvalidSearchResultTargetsAndRanges() {
        assertThrows(NullPointerException.class, () -> new SearchMatch(null, 0, 1));

        Section section = new Section("cat");

        assertThrows(IllegalArgumentException.class, () -> new SearchMatch(section, -1, 1));

        assertThrows(IllegalArgumentException.class, () -> new SearchMatch(section, 0, 4));

        assertThrows(IllegalArgumentException.class, () -> new SearchMatch(section, 1, 1));
    }
}