

public class CommandDispatcher {

/*
    - parser: CommandParser
    - engine: DocumentEngine
    - checkpoints: CheckpointManager
    - search: SearchService
    - macros: MacroManager
    - rendering: RenderService
    - statistics: DocumentStatistics
*/
    // Info of the commands in CommandDispatcher
    CommandParser parser;
    DocumentEngine engine;
    CheckpointManager checkpoints;
    SearchService search;
    MacroManager macros;
    RenderService rendering;
    DocumentStatistics statistics;

    public CommandDispatcher() {
        parser = new CommandParser();
        engine = new DocumentEngine();
        checkpoints = new SearchService();
        search = new SearchService();
        macros = new MacroManager();
        rendering = new RenderService();
        statistics = new DocumentStatistics();
    }

    public CommandResult display(String line) {
        //Displays all info of each of the commands(?)
    }

    public CommandResult replayMacro(String name) {

    }
}
