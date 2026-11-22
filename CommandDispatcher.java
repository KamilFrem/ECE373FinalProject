

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

        if (result == null) {
            System.out.println("No command result returned");
            return null;
        }
        if (result.isSuccesse()) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("Error: " + result.getMessage());
        }
    }

    public CommandResult replayMacro(String name) {
        try {
        // 1. Obtain saved Macro
        Macro macro = macros.getMacro(name);

        // 2. Create list for edit commands

        ArrayList<EditCommand> commands = new ArrayList<>();

        // 3. Go through every recorded command.

        for (String line : macro.getCommands()) {

            // Parse original command line
            ParsedCommand parsed = parser.parse(line);

            // Convert it to a editCommand object
            EditCommand command = createEditCommand(Parsed);

            // If macro contains executable edits.

            if (command == null) {
                return new CommandResult(false, "Macro contains unsupported command: " + line);
            }
            commands.add(command);
            }
            // 4. Check if macro is empty

            if (commands.isEmpty()) {
                return new CommandResult(false, "ERROR: Cannot replay empty macro.");
            }
            // 5. Combine all edits into one command.
            CompositeCommand composite = new CompositeCommand(commands);

            // 6. Execute the entire complete macro once.
            engine.execute(composite);

            // 7. Return a successful result.
            return new CommandResult(true, "Macro successfully replayed: " + name);
        }
        // in case the entire replayMacro fails, return a CommandResult showcasing a failure.
        catch (RuntimeException exception) {
            return new CommandResult(false, "Unable to replay macro: " + exception.getMessage());
        }
    }
}
