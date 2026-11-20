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

class ReplaceAllCommandTest {

    private final SearchService search = new SearchService();

    @Test
    void replacesEveryMatchWithLongerText() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat and cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "elephant",
                MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals("elephant and elephant", paragraph.getText());
    }

    @Test
    void replacesEveryMatchWithShorterText() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("elephant elephant");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "elephant", "cat",
                MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals("cat cat", paragraph.getText());

        command.undo();

        assertEquals("elephant elephant", paragraph.getText());
    }

    @Test
    void wholeWordModeSkipsMatchesInsideLargerWords() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("catalog cat cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "dog",
                MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals("catalog dog dog", paragraph.getText());
    }

    @Test
    void substringModeReplacesMatchesInsideLargerWords() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("catalog cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "dog",
                MatchMode.SUBSTRING);

        command.execute();

        assertEquals("dogalog dog", paragraph.getText());
    }

    @Test
    void oneUndoAndRedoAffectAllNestedParagraphReplacements() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph first = new Paragraph("cat and cat");

        Section nested = new Section("Details");
        Paragraph second = new Paragraph("another cat");

        section.addChild(first);
        nested.addChild(second);
        section.addChild(nested);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "elephant",
                MatchMode.WHOLE_WORD);

        engine.execute(command);

        assertEquals("elephant and elephant", first.getText());
        assertEquals("another elephant", second.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());

        engine.undo();

        assertEquals("cat and cat", first.getText());
        assertEquals("another cat", second.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(1, engine.getHistoryManager().redoCount());

        engine.redo();

        assertEquals("elephant and elephant", first.getText());
        assertEquals("another elephant", second.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());
        assertEquals(0, engine.getHistoryManager().redoCount());
    }

    @Test
    void emptyReplacementDeletesAllMatchesAndSupportsUndoRedo() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "",
                MatchMode.WHOLE_WORD);

        command.execute();

        assertEquals(" ", paragraph.getText());

        command.undo();

        assertEquals("cat cat", paragraph.getText());

        command.execute();

        assertEquals(" ", paragraph.getText());
    }

    @Test
    void replacementTextIsNotSearchedAgainDuringExecution() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "catcat",
                MatchMode.SUBSTRING);

        command.execute();

        assertEquals("catcat catcat", paragraph.getText());
    }

    @Test
    void preservesFormattingAndRestoresOriginalRunsOnUndo() {
        TextStyle regular =
                new TextStyle(false, false, "Arial", 12, "black");

        TextStyle bold =
                new TextStyle(true, false, "Arial", 12, "black");

        List<TextRun> original = new ArrayList<TextRun>();
        original.add(new TextRun("c", regular));
        original.add(new TextRun("at", bold));
        original.add(new TextRun(" and ", regular));
        original.add(new TextRun("cat", bold));
        original.add(new TextRun("!", regular));

        Paragraph paragraph = new Paragraph(original);
        Section section = new Section("Intro");
        section.addChild(paragraph);

        Document document = new Document();
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "fox",
                MatchMode.WHOLE_WORD);

        command.execute();

        List<TextRun> expected = new ArrayList<TextRun>();
        expected.add(new TextRun("fox and ", regular));
        expected.add(new TextRun("fox", bold));
        expected.add(new TextRun("!", regular));

        assertEquals("fox and fox!", paragraph.getText());
        assertEquals(expected, paragraph.getRuns());

        command.undo();

        assertEquals("cat and cat!", paragraph.getText());
        assertEquals(original, paragraph.getRuns());

        command.execute();

        assertEquals(expected, paragraph.getRuns());
    }

    @Test
    void noMatchesPreservesDocumentAndExistingRedoHistory() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);

        ReplaceAllCommand successful = new ReplaceAllCommand(
                document, search, "cat", "dog",
                MatchMode.WHOLE_WORD);

        engine.execute(successful);
        engine.undo();

        String originalText = document.getText();

        ReplaceAllCommand missing = new ReplaceAllCommand(
                document, search, "missing", "fox",
                MatchMode.SUBSTRING);

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.execute(missing));

        assertEquals(originalText, document.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(1, engine.getHistoryManager().redoCount());

        engine.redo();

        assertEquals("dog", paragraph.getText());
    }

    @Test
    void rejectsUndoBeforeExecutionAndRepeatedExecution() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat cat");

        section.addChild(paragraph);
        document.addSection(section);

        ReplaceAllCommand command = new ReplaceAllCommand(
                document, search, "cat", "dog",
                MatchMode.SUBSTRING);

        assertThrows(
                IllegalStateException.class,
                () -> command.undo());

        command.execute();

        assertThrows(
                IllegalStateException.class,
                () -> command.execute());

        assertEquals("dog dog", paragraph.getText());

        command.undo();

        assertThrows(
                IllegalStateException.class,
                () -> command.undo());

        assertEquals("cat cat", paragraph.getText());
    }

    @Test
    void rejectsInvalidConstructorArguments() {
        Document document = new Document();

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceAllCommand(
                        null, search, "cat", "dog",
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceAllCommand(
                        document, null, "cat", "dog",
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceAllCommand(
                        document, search, null, "dog",
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceAllCommand(
                        document, search, "cat", null,
                        MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> new ReplaceAllCommand(
                        document, search, "cat", "dog", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ReplaceAllCommand(
                        document, search, "", "dog",
                        MatchMode.SUBSTRING));
    }
}