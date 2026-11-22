package edu.arizona.documenteditor.cli;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleIntegrationTest {
    private final Document document = new Document();
    private final DocumentEngine engine = new DocumentEngine(document);
    private final CheckpointManager checkpoints = new CheckpointManager();
    private final RenderService rendering = new RenderService();
    private final MacroManager macros = new MacroManager();
    private final DocumentStatistics statistics = new DocumentStatistics(document);
    private final CommandDispatcher dispatcher;

    ConsoleIntegrationTest() {
        engine.addChangeListener(statistics);
        dispatcher = new CommandDispatcher(engine, checkpoints, new SearchService(),
                macros, rendering, statistics);
    }

    private String run(String line) {
        CommandResult result = dispatcher.dispatch(line);
        assertTrue(result.isSuccess(), line + ": " + result.getMessage());
        return result.getMessage();
    }

    @Test
    void createsNestedContentAndCountsParagraphsSeparately() {
        run("section \"Main\"");
        run("paragraph 1 \"Top paragraph\"");
        run("subsection 1 \"Details\"");
        run("subsection 1 \"Second\"");
        run("paragraph 1.2 \"Other paragraph\"");
        run("subsection 1.1 \"Deep\"");
        run("paragraph 1.1.1 \"Hello world\"");
        assertEquals("Main\nTop paragraph\nDetails\nDeep\nHello world\nSecond\nOther paragraph",
                run("render"));
        assertEquals(3, statistics.getParagraphCount());
        assertEquals(10, statistics.getWordCount());
        assertEquals("Words: 10\nCharacters: 60\nParagraphs: 3", run("stats"));
    }

    @Test
    void insertsDeletesAndReplacesWithUndoRedo() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("insert 1 1 5 \" world\"");
        run("replace 1 1 6 11 \"Java\"");
        run("delete 1 1 5 10");
        assertEquals("Intro\nHello", run("render"));
        run("undo");
        assertEquals("Intro\nHello Java", run("render"));
        run("redo");
        assertEquals("Intro\nHello", run("render"));
        run("replace 1 1 0 5 \"\"");
        assertEquals("", document.getParagraphs().get(0).getText());
        run("undo");
        assertEquals("Hello", document.getParagraphs().get(0).getText());
    }

    @Test
    void movesStyledTextAndUsesPremoveDestination() {
        run("section \"Intro\"");
        run("paragraph 1 \"one two three\"");
        run("format 1 1 0 3 bold true");
        run("move 1 1 0 3 1 1 13");
        assertEquals(" two threeone", document.getParagraphs().get(0).getText());
        List<TextRun> moved = document.getParagraphs().get(0).getRuns();
        assertTrue(moved.get(moved.size() - 1).getStyle().isBold());
        run("undo");
        run("paragraph 1 \"X\"");
        run("move 1 1 0 3 1 2 1");
        assertEquals("Xone", document.getParagraphs().get(1).getText());
        run("undo");
        assertEquals("one two three", document.getParagraphs().get(0).getText());
    }

    @Test
    void splitRedoKeepsLaterCommandTargetsAttached() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello world\"");
        run("split 1 1 6");
        run("insert 1 2 5 \"!\"");
        run("undo");
        run("undo");
        run("redo");
        run("redo");
        assertEquals("Intro\nHello\nworld!", run("render"));
        run("merge 1 1");
        assertEquals("Hello world!", document.getParagraphs().get(0).getText());
        run("undo");
        assertEquals(2, document.getParagraphs().size());
    }

    @Test
    void mergeRejectsASectionBetweenParagraphs() {
        run("section \"Main\"");
        run("paragraph 1 \"First\"");
        run("subsection 1 \"Between\"");
        run("paragraph 1 \"Second\"");
        String before = document.getText();
        assertFalse(dispatcher.dispatch("merge 1 1").isSuccess());
        assertEquals(before, document.getText());
    }

    @Test
    void changesAllFormattingPropertiesOnTitlesAndParagraphs() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("format 1 1 0 5 bold true");
        run("format 1 1 0 5 italic true");
        run("format 1 1 0 5 fontName \"Courier New\"");
        run("format 1 1 0 5 fontSize 20");
        run("format 1 1 0 5 color blue");
        TextStyle style = document.getParagraphs().get(0).getRuns().get(0).getStyle();
        assertEquals(new TextStyle(true, true, "Courier New", 20, "blue"), style);
        TextStyle titleBefore = document.getSections().get(0).getRuns().get(0).getStyle();
        run("titleformat 1 0 5 bold true");
        run("view marked");
        assertTrue(run("render").contains("[bold]Intro[/bold]"));
        assertTrue(run("render").contains("[bold][italic]Hello[/italic][/bold]"));
        run("view plain");
        assertEquals("Intro\nHello", run("render"));
        run("undo");
        assertEquals(titleBefore, document.getSections().get(0).getRuns().get(0).getStyle());
    }

    @Test
    void selectsWidthAndWrappingWithoutAddingHistory() {
        run("section \"Intro\"");
        run("paragraph 1 \"aaa bb cc ddddd\"");
        int history = engine.getHistoryManager().undoCount();
        run("width 10");
        assertEquals("Intro\naaa bb cc\nddddd", run("render"));
        run("wrap balanced");
        assertEquals("Intro\naaa bb\ncc ddddd", run("render"));
        run("wrap greedy");
        assertEquals(history, engine.getHistoryManager().undoCount());
    }

    @Test
    void findsTitlesAndParagraphsWithBothMatchingModes() {
        run("section \"cat title\"");
        run("subsection 1 \"Nested\"");
        run("paragraph 1.1 \"catalog cat\"");
        assertEquals("1. Section 1 title [0, 3)\n"
                + "2. Section 1.1 paragraph 1 [8, 11)", run("find cat word"));
        assertEquals(3, run("find cat substring").split("\n").length);
        assertEquals("No matches found.", run("find missing"));
        int history = engine.getHistoryManager().undoCount();
        assertEquals("No matches found.", dispatcher.dispatch("replacefirst missing dog").getMessage());
        assertEquals(history, engine.getHistoryManager().undoCount());
        run("replacefirst cat fox word");
        assertEquals("fox title", document.getSections().get(0).getText());
        run("replaceall cat dog substring");
        assertEquals("dogalog dog", document.getParagraphs().get(0).getText());
        run("undo");
        assertEquals("catalog cat", document.getParagraphs().get(0).getText());
    }

    @Test
    void failedRequestsPreserveRedoAndDoNotPartiallyEdit() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("undo");
        String before = document.getText();
        String[] invalid = {"paragraph 1.9 Nope", "subsection 1.0 Nope",
                "paragraph 1..1 Nope", "paragraph 1. Nope", "width 0",
                "view missing", "wrap missing", "find cat invalid", "unknown"};
        for (int i = 0; i < invalid.length; i++) {
            assertFalse(dispatcher.dispatch(invalid[i]).isSuccess(), invalid[i]);
            assertEquals(before, document.getText());
            assertEquals(1, engine.getHistoryManager().redoCount());
        }
        run("redo");
        int history = engine.getHistoryManager().undoCount();
        assertFalse(dispatcher.dispatch("delete 1 1 0 99").isSuccess());
        assertFalse(dispatcher.dispatch("format 1 1 0 5 bold yes").isSuccess());
        assertEquals(history, engine.getHistoryManager().undoCount());
        assertEquals("Hello", document.getParagraphs().get(0).getText());
    }

    @Test
    void undoAcrossRestoreUpdatesTheActualDocumentAndStatistics() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("checkpoint original");
        run("insert 1 1 5 \" world\"");
        run("restore original");
        run("undo");
        run("undo");
        assertEquals("Intro\nHello", document.getText());
        assertEquals(2, statistics.getWordCount());
        run("redo");
        run("redo");
        assertEquals("Intro\nHello", document.getText());
        run("insert 1 1 5 \"!\"");
        run("undo");
        run("undo");
        assertEquals("Intro\nHello world", document.getText());
    }

    @Test
    void checkpointSavePreservesRedoAndRequiresExplicitOverwrite() {
        run("section \"Intro\"");
        run("undo");
        run("checkpoint empty");
        assertTrue(engine.canRedo());
        run("redo");
        assertFalse(dispatcher.dispatch("checkpoint empty").isSuccess());
        run("checkpoint empty overwrite");
        run("restore empty");
        assertEquals("Intro", document.getText());
        assertEquals("empty", run("checkpoints"));
    }

    @Test
    void completeDocumentCanRenderAndSearchOver100000Characters() {
        run("section \"Capacity\"");
        String text = "alpha beta ".repeat(10000).strip();
        run("paragraph 1 \"" + text + "\"");
        run("width 50");
        String output = run("render");
        String paragraphOutput = output.substring(output.indexOf('\n') + 1);
        assertEquals(text, paragraphOutput.replace('\n', ' '));
        assertEquals(10000, run("find beta word").split("\n").length);
        assertEquals(20001, statistics.getWordCount());
        run("wrap balanced");
        String balanced = run("render");
        assertEquals(text, balanced.substring(balanced.indexOf('\n') + 1).replace('\n', ' '));
    }
}
