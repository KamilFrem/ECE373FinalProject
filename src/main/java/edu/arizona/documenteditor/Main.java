package edu.arizona.documenteditor;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.cli.CommandDispatcher;
import edu.arizona.documenteditor.cli.CommandLineInterface;
import edu.arizona.documenteditor.cli.CommandResult;
import edu.arizona.documenteditor.cli.ScriptRunner;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;

/** Creates shared services and starts console or script operation. */
public final class Main {
    private Main() {
    }

    /**
     * Starts the console, or runs one script when a path argument is supplied.
     * @param args zero arguments, or one script path
     */
    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("Usage: Main [scriptPath]");
            return;
        }
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();
        RenderService rendering = new RenderService();
        SearchService search = new SearchService();
        MacroManager macros = new MacroManager();
        DocumentStatistics statistics = new DocumentStatistics(document);
        engine.addChangeListener(statistics);

        CommandDispatcher dispatcher = new CommandDispatcher(
                engine, checkpoints, search, macros, rendering, statistics);
        CommandLineInterface console = new CommandLineInterface(dispatcher);
        if (args.length == 1) {
            CommandResult result = new ScriptRunner(dispatcher).run(args[0]);
            console.display(result);
        } else {
            System.out.println("ECE 373 Document Editor");
            console.run();
        }
    }
}
