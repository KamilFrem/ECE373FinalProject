package edu.arizona.documenteditor.cli;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandDispatcherTest {

    private final Document document = new Document();
    private final DocumentEngine engine = new DocumentEngine(document);

    private final DocumentStatistics statistics =
            new DocumentStatistics(document);

    private final CommandDispatcher dispatcher;

    CommandDispatcherTest() {
        engine.addChangeListener(statistics);

        dispatcher = new CommandDispatcher(
                engine,
                new CheckpointManager(),
                new SearchService(),
                new MacroManager(),
                new RenderService(),
                statistics);
    }

    @Test
    void createsContentAndReportsStatistics() {
        assertTrue(
                dispatcher.dispatch("section \"Introduction\"").isSuccess());

        assertTrue(
                dispatcher.dispatch(
                        "paragraph 1 \"Hello world\"").isSuccess());

        CommandResult rendered = dispatcher.dispatch("render");

        assertTrue(rendered.isSuccess());
        assertEquals("Introduction\nHello world", rendered.getMessage());

        CommandResult counts = dispatcher.dispatch("stats");

        assertTrue(counts.isSuccess());
        assertEquals(
                "Words: 3\nCharacters: 23\nParagraphs: 1",
                counts.getMessage());
    }

    @Test
    void undoAndRedoUpdateContentAndStatistics() {
        dispatcher.dispatch("section \"Introduction\"");
        dispatcher.dispatch("paragraph 1 \"Hello world\"");

        assertTrue(dispatcher.dispatch("undo").isSuccess());

        assertEquals(
                "Introduction",
                dispatcher.dispatch("render").getMessage());

        assertEquals(
                "Words: 1\nCharacters: 12\nParagraphs: 0",
                dispatcher.dispatch("stats").getMessage());

        assertTrue(dispatcher.dispatch("redo").isSuccess());

        assertEquals(
                "Introduction\nHello world",
                dispatcher.dispatch("render").getMessage());

        assertEquals(
                "Words: 3\nCharacters: 23\nParagraphs: 1",
                dispatcher.dispatch("stats").getMessage());
    }

    @Test
    void invalidSectionPreservesDocumentAndRedoHistory() {
        dispatcher.dispatch("section \"Introduction\"");
        dispatcher.dispatch("paragraph 1 \"Hello world\"");
        dispatcher.dispatch("undo");

        int undoCount = engine.getHistoryManager().undoCount();
        int redoCount = engine.getHistoryManager().redoCount();
        String originalText = document.getText();

        CommandResult result =
                dispatcher.dispatch("paragraph 9 \"Invalid\"");

        assertFalse(result.isSuccess());
        assertEquals("Section number does not exist.", result.getMessage());

        assertEquals(originalText, document.getText());
        assertEquals(undoCount, engine.getHistoryManager().undoCount());
        assertEquals(redoCount, engine.getHistoryManager().redoCount());

        assertTrue(dispatcher.dispatch("redo").isSuccess());
        assertEquals("Introduction\nHello world", document.getText());
    }

    @Test
    void rejectsInvalidCommandsWithoutAddingHistory() {
        assertFalse(dispatcher.dispatch("hello").isSuccess());
        assertFalse(dispatcher.dispatch("section").isSuccess());
        assertFalse(
                dispatcher.dispatch("paragraph abc \"Hello\"").isSuccess());
        assertFalse(dispatcher.dispatch("stats extra").isSuccess());
        assertFalse(dispatcher.dispatch("undo").isSuccess());
        assertFalse(dispatcher.dispatch("redo").isSuccess());

        assertEquals("", document.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(0, engine.getHistoryManager().redoCount());
    }

    @Test
    void createsParagraphsAtMultipleSectionDepths() {
        assertTrue(
                dispatcher.dispatch("section \"Main\"").isSuccess());

        assertTrue(
                dispatcher.dispatch(
                        "paragraph 1 \"Top paragraph\"").isSuccess());

        assertTrue(
                dispatcher.dispatch(
                        "subsection 1 \"Details\"").isSuccess());

        assertTrue(
                dispatcher.dispatch(
                        "paragraph 1.1 \"Nested paragraph\"").isSuccess());

        assertTrue(
                dispatcher.dispatch(
                        "subsection 1.1 \"More details\"").isSuccess());

        assertTrue(
                dispatcher.dispatch(
                        "paragraph 1.1.1 \"Deep paragraph\"").isSuccess());

        assertEquals(
                "Main\nTop paragraph\nDetails\nNested paragraph"
                        + "\nMore details\nDeep paragraph",
                dispatcher.dispatch("render").getMessage());
    }

    @Test
    void countsOnlySubsectionsWhenResolvingSectionPaths() {
        dispatcher.dispatch("section \"Main\"");
        dispatcher.dispatch("paragraph 1 \"First paragraph\"");
        dispatcher.dispatch("subsection 1 \"First subsection\"");
        dispatcher.dispatch("paragraph 1 \"Second paragraph\"");
        dispatcher.dispatch("subsection 1 \"Second subsection\"");

        CommandResult result =
                dispatcher.dispatch("paragraph 1.2 \"Target paragraph\"");

        assertTrue(result.isSuccess());

        assertEquals(
                "Main\nFirst paragraph\nFirst subsection"
                        + "\nSecond paragraph\nSecond subsection"
                        + "\nTarget paragraph",
                dispatcher.dispatch("render").getMessage());
    }

    @Test
    void undoesAndRedoesSubsectionCreation() {
        dispatcher.dispatch("section \"Main\"");
        dispatcher.dispatch("subsection 1 \"Details\"");

        assertEquals("Main\nDetails", document.getText());

        assertTrue(dispatcher.dispatch("undo").isSuccess());
        assertEquals("Main", document.getText());

        assertTrue(dispatcher.dispatch("redo").isSuccess());
        assertEquals("Main\nDetails", document.getText());

        assertTrue(
                dispatcher.dispatch(
                        "paragraph 1.1 \"Restored subsection\"").isSuccess());

        assertEquals(
                "Main\nDetails\nRestored subsection",
                document.getText());
    }

    @Test
    void invalidNestedPathsPreserveDocumentAndHistory() {
        dispatcher.dispatch("section \"Main\"");

        String originalText = document.getText();
        int originalUndoCount = engine.getHistoryManager().undoCount();

        assertFalse(
                dispatcher.dispatch(
                        "paragraph 1.9 \"Invalid\"").isSuccess());

        assertFalse(
                dispatcher.dispatch(
                        "subsection 1.0 \"Invalid\"").isSuccess());

        assertFalse(
                dispatcher.dispatch(
                        "paragraph 1..1 \"Invalid\"").isSuccess());

        assertFalse(
                dispatcher.dispatch(
                        "paragraph 1. \"Invalid\"").isSuccess());

        assertEquals(originalText, document.getText());

        assertEquals(
                originalUndoCount,
                engine.getHistoryManager().undoCount());
    }
}