package edu.arizona.documenteditor.cli;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.macro.Macro;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MacroReplayIntegrationTest {
    private final Document document = new Document();
    private final DocumentEngine engine = new DocumentEngine(document);
    private final CheckpointManager checkpoints = new CheckpointManager();
    private final RenderService rendering = new RenderService();
    private final MacroManager macros = new MacroManager();
    private final DocumentStatistics statistics = new DocumentStatistics(document);
    private final CommandDispatcher dispatcher;

    MacroReplayIntegrationTest() {
        engine.addChangeListener(statistics);
        dispatcher = new CommandDispatcher(engine, checkpoints, new SearchService(),
                macros, rendering, statistics);
    }

    private String run(String line) {
        CommandResult result = dispatcher.dispatch(line);
        assertTrue(result.isSuccess(), line + ": " + result.getMessage());
        return result.getMessage();
    }

    private void save(String name, String... lines) {
        macros.startRecording(name);
        for (int i = 0; i < lines.length; i++) {
            macros.record(lines[i]);
        }
        macros.stopRecording();
    }

    @Test
    void recordsSuccessfulMixedCommandsAndReplaysAsOneHistoryEntry() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("record style");
        run("format 1 1 0 5 bold true");
        run("width 10");
        run("view marked");
        run("render");
        assertFalse(dispatcher.dispatch("width 0").isSuccess());
        run("stop");
        assertEquals(4, macros.getMacro("style").getCommands().size());
        run("undo");
        run("view plain");
        run("width 80");
        int before = engine.getHistoryManager().undoCount();
        assertTrue(run("replay style").contains("[bold]Hello[/bold]"));
        assertEquals(before + 1, engine.getHistoryManager().undoCount());
        assertEquals(10, rendering.getWidth());
        run("undo");
        assertEquals(80, rendering.getWidth());
        assertFalse(document.getParagraphs().get(0).getRuns().get(0).getStyle().isBold());
        assertEquals("Intro\nHello", run("render"));
        run("redo");
        assertEquals(10, rendering.getWidth());
        assertTrue(run("render").contains("[bold]Hello[/bold]"));
    }

    @Test
    void deferredLookupSupportsContentCreatedEarlierInTheMacro() {
        save("build", "section \"Main\"", "subsection 1 \"Nested\"",
                "paragraph 1.1 \"Hello\"", "insert 1.1 1 5 \" world\"", "stats");
        String output = run("replay build");
        assertTrue(output.contains("Words: 4"));
        assertEquals("Main\nNested\nHello world", document.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());
        run("insert 1.1 1 11 \"!\"");
        run("undo");
        run("undo");
        assertEquals("", document.getText());
        run("redo");
        run("redo");
        assertEquals("Main\nNested\nHello world!", document.getText());
    }

    @Test
    void splitTargetsRemainValidAcrossMacroUndoAndRedo() {
        run("section \"Main\"");
        run("paragraph 1 \"Hello world\"");
        save("split", "split 1 1 6", "insert 1 2 5 \"!\"");
        run("replay split");
        run("undo");
        run("redo");
        assertEquals("Main\nHello \nworld!", document.getText());
        run("undo");
        assertEquals("Main\nHello world", document.getText());
    }

    @Test
    void failureRollsBackEditsSettingsCheckpointsAndPreservesRedo() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("checkpoint existing");
        run("insert 1 1 5 \"!\"");
        run("undo");
        String before = document.getText();
        int undoCount = engine.getHistoryManager().undoCount();
        save("fail", "insert 1 1 5 \" world\"", "width 5", "view marked",
                "checkpoint temporary", "checkpoint existing overwrite", "insert 1 1 99 \"Bad\"");
        CommandResult result = dispatcher.dispatch("replay fail");
        assertFalse(result.isSuccess());
        assertEquals(before, document.getText());
        assertEquals(80, rendering.getWidth());
        assertEquals("Intro\nHello", run("render"));
        assertFalse(checkpoints.hasCheckpoint("temporary"));
        assertEquals(before, checkpoints.getCheckpoint("existing").getDocument().getText());
        assertEquals(undoCount, engine.getHistoryManager().undoCount());
        assertEquals(1, engine.getHistoryManager().redoCount());
        assertEquals(2, statistics.getWordCount());
        run("redo");
        assertEquals("Intro\nHello!", document.getText());
    }

    @Test
    void checkpointRestoreInsideMacroRemainsUndoable() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("checkpoint original");
        run("insert 1 1 5 \" world\"");
        save("restore", "restore original", "insert 1 1 5 \"!\"");
        run("replay restore");
        assertEquals("Intro\nHello!", document.getText());
        run("undo");
        run("undo");
        assertEquals("Intro\nHello", document.getText());
        run("redo");
        run("redo");
        assertEquals("Intro\nHello!", document.getText());
    }

    @Test
    void failureAfterRestoreReattachesTheOriginalTargets() {
        run("section \"Intro\"");
        run("paragraph 1 \"Hello\"");
        run("checkpoint original");
        run("insert 1 1 5 \" world\"");
        save("badrestore", "restore original", "insert 1 1 99 \"Bad\"");
        assertFalse(dispatcher.dispatch("replay badrestore").isSuccess());
        assertEquals("Intro\nHello world", document.getText());
        run("undo");
        assertEquals("Intro\nHello", document.getText());
    }

    @Test
    void presentationOnlyMacroIsUndoableAndRedoable() {
        save("settings", "width 12", "wrap balanced", "view marked");
        run("replay settings");
        assertEquals(12, rendering.getWidth());
        assertEquals(1, engine.getHistoryManager().undoCount());
        run("undo");
        assertEquals(80, rendering.getWidth());
        run("redo");
        assertEquals(12, rendering.getWidth());
    }

    @Test
    void rejectsEmptyMissingAndNestedMacrosWithoutChangingHistory() {
        save("empty");
        assertFalse(dispatcher.dispatch("replay empty").isSuccess());
        assertFalse(dispatcher.dispatch("replay missing").isSuccess());
        save("nested", "section \"Temporary\"", "replay empty");
        assertFalse(dispatcher.dispatch("replay nested").isSuccess());
        assertEquals("", document.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
    }

    @Test
    void preventsHistoryControlDuringRecording() {
        run("section \"Intro\"");
        run("record example");
        assertFalse(dispatcher.dispatch("undo").isSuccess());
        assertFalse(dispatcher.dispatch("redo").isSuccess());
        assertFalse(dispatcher.dispatch("replay example").isSuccess());
        run("stop");
        assertEquals("Intro", document.getText());
        assertEquals(0, macros.getMacro("example").getCommands().size());
    }
}
