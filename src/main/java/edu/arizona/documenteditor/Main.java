package edu.arizona.documenteditor;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;

/** Starts the document editor. */
public final class Main {

    private Main() {
    }

    /**
     * Creates the document and application services.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        Document document = new Document();

        DocumentEngine engine = new DocumentEngine(document);
        CheckpointManager checkpoints = new CheckpointManager();
        RenderService renderService = new RenderService();
        SearchService search = new SearchService();
        MacroManager macros = new MacroManager();

        DocumentStatistics statistics = new DocumentStatistics(document);
        engine.addChangeListener(statistics);

        System.out.println("ECE 373 Document Editor");
    }
}
