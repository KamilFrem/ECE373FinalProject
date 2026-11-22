package edu.arizona.documenteditor.cli;

import edu.arizona.documenteditor.Main;
import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ScriptRunnerTest {
    @TempDir
    Path directory;

    private CommandDispatcher dispatcher(Document document) {
        DocumentEngine engine = new DocumentEngine(document);
        DocumentStatistics statistics = new DocumentStatistics(document);
        engine.addChangeListener(statistics);
        return new CommandDispatcher(engine, new CheckpointManager(), new SearchService(),
                new MacroManager(), new RenderService(), statistics);
    }

    @Test
    void consoleAndScriptProduceEquivalentStateAndOutput() throws Exception {
        String commands = "section \"Intro\"\nparagraph 1 \"Hello world\"\n"
                + "format 1 1 0 5 bold true\nview marked\nrender\n"
                + "find world word\nstats\nundo\nredo\n";
        Path path = directory.resolve("commands.txt");
        Files.writeString(path, commands);
        Document scripted = new Document();
        CommandResult script = new ScriptRunner(dispatcher(scripted)).run(path.toString());
        assertTrue(script.isSuccess(), script.getMessage());

        Document interactive = new Document();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            new CommandLineInterface(dispatcher(interactive),
                    new Scanner(commands + "exit\n")).run();
        } finally {
            System.setOut(original);
        }
        String console = bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        console = console.replace("Type help for commands, or exit to quit.\n", "");
        console = console.replace("> ", "").stripTrailing();
        assertEquals(script.getMessage(), console);
        assertEquals(scripted.getText(), interactive.getText());
        assertEquals(scripted.getParagraphs().get(0).getRuns(),
                interactive.getParagraphs().get(0).getRuns());
    }

    @Test
    void acceptsBomCommentsAndBlankLines() throws Exception {
        Path path = directory.resolve("comments.txt");
        Files.writeString(path, "\uFEFF# Heading\n\nsection \"Intro\"\n  # comment\n");
        Document document = new Document();
        CommandResult result = new ScriptRunner(dispatcher(document)).run(path.toString());
        assertTrue(result.isSuccess());
        assertEquals("Intro", document.getText());
    }

    @Test
    void errorReportsTheLineAndKeepsEarlierSuccessfulCommands() throws Exception {
        Path path = directory.resolve("bad.txt");
        Files.writeString(path, "section \"Intro\"\nparagraph 9 \"Bad\"\nsection \"Never\"\n");
        Document document = new Document();
        CommandResult result = new ScriptRunner(dispatcher(document)).run(path.toString());
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Line 2:"));
        assertEquals("Intro", document.getText());
    }

    @Test
    void mainRunsAQuotedPathWithoutWaitingForConsoleInput() throws Exception {
        Path path = directory.resolve("script with spaces.txt");
        Files.writeString(path, "section \"Intro\"\nparagraph 1 \"Hello\"\nrender\n");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(bytes, true, StandardCharsets.UTF_8));
            Main.main(new String[]{path.toString()});
        } finally {
            System.setOut(original);
        }
        String output = bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        assertTrue(output.contains("Intro\nHello"));
        assertFalse(output.contains("Type help"));
    }

    @Test
    void rejectsNestedScriptsAndReportsMissingFiles() throws Exception {
        CommandDispatcher dispatcher = dispatcher(new Document());
        assertFalse(new ScriptRunner(dispatcher).run(directory.resolve("missing").toString()).isSuccess());
        Path path = directory.resolve("nested.txt");
        Files.writeString(path, "script \"anything.txt\"\n");
        assertFalse(new ScriptRunner(dispatcher).run(path.toString()).isSuccess());
    }

    @Test
    void consoleExitsCleanlyAtEndOfInput() {
        assertDoesNotThrow(() -> new CommandLineInterface(
                dispatcher(new Document()), new Scanner("")).run());
    }
}
