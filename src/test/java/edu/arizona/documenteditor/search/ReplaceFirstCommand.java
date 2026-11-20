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
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplaceFirstCommandTest {

    private final SearchService search = new SearchService();

    @Test
    void replacesOnlyTheFirstMatch() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat and cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "dog", MatchMode.SUBSTRING);

        command.execute();

        assertEquals("dog and cat", paragraph.getText());
    }

    @Test
    void wholeWordModeSkipsMatchesInsideLargerWords() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("catalog cat cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "dog", MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals("catalog dog cat", paragraph.getText());
    }

    @Test
    void replacesTheFirstMatchInNestedDocumentOrder() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph first = new Paragraph("No matching animal");

        Section nested = new Section("Details");
        Paragraph second = new Paragraph("cat here");
        Paragraph third = new Paragraph("cat later");

        section.addChild(first);
        nested.addChild(second);
        section.addChild(nested);
        section.addChild(third);
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "dog", MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals("No matching animal", first.getText());
        assertEquals("dog here", second.getText());
        assertEquals("cat later", third.getText());
    }

    @Test
    void supportsUndoAndRedoThroughTheEngine() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat and cat");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "elephant",
                MatchMode.WHOLE_WORD);

        engine.execute(command);

        assertEquals("elephant and cat", paragraph.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());

        engine.undo();

        assertEquals("cat and cat", paragraph.getText());

        engine.redo();

        assertEquals("elephant and cat", paragraph.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());
    }

    @Test
    void emptyReplacementDeletesTheMatchAndCanBeUndone() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat dog");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "", MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals(" dog", paragraph.getText());

        command.undo();

        assertEquals("cat dog", paragraph.getText());

        command.execute();

        assertEquals(" dog", paragraph.getText());
    }

    @Test
    void preservesUnrelatedFormattingAndRestoresMatchedStyles() {
        TextStyle regular =
                new TextStyle(false, false, "Arial", 12, "black");

        TextStyle bold =
                new TextStyle(true, false, "Arial", 12, "black");

        List<TextRun> original = new ArrayList<TextRun>();
        original.add(new TextRun("c", regular));
        original.add(new TextRun("at", bold));
        original.add(new TextRun(" dog", regular));

        Paragraph paragraph = new Paragraph(original);
        Section section = new Section("Intro");
        section.addChild(paragraph);

        Document document = new Document();
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "fox", MatchMode.WHOLE_WORD);

        command.execute();

        // Replacement inherits the first matched character's style.
        assertEquals("fox dog", paragraph.getText());

        List<TextRun> expected = new ArrayList<TextRun>();
        expected.add(new TextRun("fox dog", regular));

        assertEquals(expected, paragraph.getRuns());

        command.undo();

        assertEquals("cat dog", paragraph.getText());
        assertEquals(original, paragraph.getRuns());
    }

    @Test
    void noMatchesPreservesDocumentAndHistory() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("Hello world");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);
        String originalText = document.getText();

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "missing", "dog",
                MatchMode.SUBSTRING);

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.execute(command));

        assertEquals(originalText, document.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(0, engine.getHistoryManager().redoCount());
    }

    @Test
    void rejectsUndoBeforeExecutionAndRepeatedExecution() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceFirstCommand command = new ReplaceFirstCommand(
                document, search, "cat", "dog", MatchMode.SUBSTRING);

        assertThrows(
                IllegalStateException.class,
                () -> command.undo());

        command.execute();

        assertThrows(
                IllegalStateException.class,
                () -> command.execute());

        assertEquals("dog", paragraph.getText());

        command.undo();

        assertThrows(
                IllegalStateException.class,
                () -> command.undo());

        assertEquals("cat", paragraph.getText());
    }

    @Test
    void rejectsInvalidConstructorArguments() {
        Document document = new Document();

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceFirstCommand(
                        null, search, "cat", "dog",
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceFirstCommand(
                        document, null, "cat", "dog",
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceFirstCommand(
                        document, search, null, "dog",
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceFirstCommand(
                        document, search, "cat", null,
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceFirstCommand(
                        document, search, "cat", "dog", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReplaceFirstCommand(
                        document, search, "", "dog",
                        MatchMode.SUBSTRING));
    }
}