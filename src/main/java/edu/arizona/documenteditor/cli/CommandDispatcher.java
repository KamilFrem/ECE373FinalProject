package edu.arizona.documenteditor.cli;

import edu.arizona.documenteditor.checkpoint.CheckpointManager;
import edu.arizona.documenteditor.checkpoint.RestoreCheckpointCommand;
import edu.arizona.documenteditor.command.CreateParagraphCommand;
import edu.arizona.documenteditor.command.CreateSectionCommand;
import edu.arizona.documenteditor.command.DeleteTextCommand;
import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.command.InsertTextCommand;
import edu.arizona.documenteditor.command.MergeParagraphsCommand;
import edu.arizona.documenteditor.command.MoveTextCommand;
import edu.arizona.documenteditor.command.ReplaceTextCommand;
import edu.arizona.documenteditor.command.SplitParagraphCommand;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.formatting.FormatCommand;
import edu.arizona.documenteditor.macro.MacroManager;
import edu.arizona.documenteditor.model.DocumentNode;
import edu.arizona.documenteditor.model.FormatTarget;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.presentation.BalancedLineBreaker;
import edu.arizona.documenteditor.presentation.GreedyLineBreaker;
import edu.arizona.documenteditor.presentation.MarkedUpRenderer;
import edu.arizona.documenteditor.presentation.PlainTextRenderer;
import edu.arizona.documenteditor.presentation.RenderService;
import edu.arizona.documenteditor.search.MatchMode;
import edu.arizona.documenteditor.search.ReplaceAllCommand;
import edu.arizona.documenteditor.search.ReplaceFirstCommand;
import edu.arizona.documenteditor.search.SearchMatch;
import edu.arizona.documenteditor.search.SearchService;
import edu.arizona.documenteditor.statistics.DocumentStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Validates console requests and connects them to shared application services. */
public final class CommandDispatcher {
    private final CommandParser parser;
    private final DocumentEngine engine;
    private final CheckpointManager checkpoints;
    private final SearchService search;
    private final MacroManager macros;
    private final RenderService rendering;
    private final DocumentStatistics statistics;

    /**
     * Creates a dispatcher using the services created by Main.
     * @param engine shared document engine
     * @param checkpoints shared checkpoint manager
     * @param search shared search service
     * @param macros shared macro manager
     * @param rendering shared rendering settings
     * @param statistics statistics registered with the engine
     * @throws NullPointerException if a service is null
     */
    public CommandDispatcher(DocumentEngine engine, CheckpointManager checkpoints, SearchService search, MacroManager macros, RenderService rendering, DocumentStatistics statistics) {

        if (engine == null || checkpoints == null || search == null || macros == null || rendering == null || statistics == null) {
            throw new NullPointerException("Application services cannot be null.");
        }
        parser = new CommandParser();
        this.engine = engine;
        this.checkpoints = checkpoints;
        this.search = search;
        this.macros = macros;
        this.rendering = rendering;
        this.statistics = statistics;
    }

    /**
     * Executes one request and records successful ordinary commands when recording.
     * @param line complete command line
     * @return success output or a short error explanation
     */
    public CommandResult dispatch(String line) {
        try {
            ParsedCommand command = parser.parse(line);
            String name = command.getName();
            ArrayList<String> arguments = command.getArguments();

            if (name.equals("record")) {
                requireCount(command, 1, "record name");
                macros.startRecording(arguments.get(0));
                return new CommandResult(true, "Recording macro: " + arguments.get(0));
            }
            if (name.equals("stop")) {
                requireCount(command, 0, "stop");
                macros.stopRecording();
                return new CommandResult(true, "Macro saved.");
            }
            if (macros.isRecording() && (name.equals("undo") || name.equals("redo") || name.equals("replay"))) {

                throw new IllegalArgumentException("Stop recording before undo, redo, or replay.");
            }
            if (name.equals("undo")) {
                requireCount(command, 0, "undo");

                if (!engine.canUndo()) {
                    return new CommandResult(false, "Nothing to undo.");
                }

                engine.undo();
                return new CommandResult(true, "Change undone.");
            }
            if (name.equals("redo")) {

                requireCount(command, 0, "redo");

                if (!engine.canRedo()) {
                    return new CommandResult(false, "Nothing to redo.");
                }
                engine.redo();
                return new CommandResult(true, "Change redone.");
            }
            if (name.equals("replay")) {

                requireCount(command, 1, "replay name");
                MacroReplayCommand replay = new MacroReplayCommand(
                        this, macros.getMacro(arguments.get(0)).getCommands());
                engine.execute(replay);
                return new CommandResult(true, replay.getOutput());
            }
            if (name.equals("script")) {

                requireCount(command, 1, "script \"Path\"");
                return new ScriptRunner(this).run(arguments.get(0));
            }

            EditCommand edit = createEditCommand(command);
            String message;

            if (edit != null) {
                engine.execute(edit);
                message = editMessage(name);
            }
            else {
                message = executeNonEdit(command);
            }
            if (macros.isRecording()) {
                macros.record(line);
            }
            return new CommandResult(true, message);
        }
        catch (NumberFormatException exception) {

            return new CommandResult(false, "Use whole numbers for positions, sizes, and paragraph numbers. " + "Section paths use dots, such as 1.2.");
        }
        catch (RuntimeException exception) {

            String message = exception.getMessage();
            if (message == null) {

                message = exception.getClass().getSimpleName();
            }

            return new CommandResult(false, message);
        }
    }

    // Resolving targets here, immediately before execution, also supports macros
    // that create a section and then address it in a later recorded command.
    EditCommand createEditCommand(ParsedCommand command) {
        String name = command.getName();
        ArrayList<String> a = command.getArguments();
        if (name.equals("section")) {
            requireCount(command, 1, "section \"Title\"");
            return new CreateSectionCommand(engine.getDocument(),
                    engine.getDocument().getSections().size(), a.get(0));
        }
        if (name.equals("subsection")) {
            requireCount(command, 2, "subsection parentPath \"Title\"");
            Section parent = findSection(a.get(0));
            return new CreateSectionCommand(parent, parent.getChildren().size(), a.get(1));
        }
        if (name.equals("paragraph")) {
            requireCount(command, 2, "paragraph sectionPath \"Text\"");
            Section parent = findSection(a.get(0));
            return new CreateParagraphCommand(parent, parent.getChildren().size(), a.get(1));
        }
        if (name.equals("insert")) {
            requireCount(command, 4, "insert sectionPath paragraphNumber position \"Text\"");
            return new InsertTextCommand(findParagraph(a.get(0), number(a.get(1))),
                    number(a.get(2)), a.get(3));
        }
        if (name.equals("delete")) {
            requireCount(command, 4, "delete sectionPath paragraphNumber start end");
            return new DeleteTextCommand(findParagraph(a.get(0), number(a.get(1))),
                    number(a.get(2)), number(a.get(3)));
        }
        if (name.equals("replace")) {
            requireCount(command, 5, "replace sectionPath paragraphNumber start end \"Text\"");
            return new ReplaceTextCommand(findParagraph(a.get(0), number(a.get(1))),
                    number(a.get(2)), number(a.get(3)), a.get(4));
        }
        if (name.equals("move")) {
            requireCount(command, 7,
                    "move sourceSection sourceParagraph start end destinationSection destinationParagraph position");
            return new MoveTextCommand(findParagraph(a.get(0), number(a.get(1))),
                    number(a.get(2)), number(a.get(3)),
                    findParagraph(a.get(4), number(a.get(5))), number(a.get(6)));
        }
        if (name.equals("split")) {
            requireCount(command, 3, "split sectionPath paragraphNumber position");
            return new SplitParagraphCommand(findParagraph(a.get(0), number(a.get(1))),
                    number(a.get(2)));
        }
        if (name.equals("merge")) {
            requireCount(command, 2, "merge sectionPath firstParagraphNumber");
            Section parent = findSection(a.get(0));
            Paragraph first = findParagraph(a.get(0), number(a.get(1)));
            List<DocumentNode> children = parent.getChildren();
            int next = children.indexOf(first) + 1;
            if (next >= children.size() || children.get(next).getClass() != Paragraph.class) {
                throw new IllegalArgumentException("The next child must be a paragraph.");
            }
            return new MergeParagraphsCommand(parent, first, (Paragraph) children.get(next));
        }
        if (name.equals("format")) {
            requireCount(command, 6, "format sectionPath paragraphNumber start end attribute value");
            return new FormatCommand(findParagraph(a.get(0), number(a.get(1))),
                    number(a.get(2)), number(a.get(3)), a.get(4), a.get(5));
        }
        if (name.equals("titleformat")) {
            requireCount(command, 5, "titleformat sectionPath start end attribute value");
            return new FormatCommand(findSection(a.get(0)), number(a.get(1)),
                    number(a.get(2)), a.get(3), a.get(4));
        }
        if (name.equals("replacefirst") || name.equals("replaceall")) {
            requireRange(command, 2, 3, name + " \"Query\" \"Replacement\" [substring|word]");
            MatchMode mode = MatchMode.SUBSTRING;
            if (a.size() == 3) {
                mode = matchMode(a.get(2));
            }
            if (name.equals("replacefirst")) {
                return new ReplaceFirstCommand(engine.getDocument(), search,
                        a.get(0), a.get(1), mode);
            }
            return new ReplaceAllCommand(engine.getDocument(), search,
                    a.get(0), a.get(1), mode);
        }
        if (name.equals("restore")) {
            requireCount(command, 1, "restore name");
            return new RestoreCheckpointCommand(engine.getDocument(), checkpoints, a.get(0));
        }
        return null;
    }

    // These operations do not enter ordinary document history. Macro replay
    // captures their settings/checkpoint effects so a failed replay can roll back.
    String executeNonEdit(ParsedCommand command) {
        String name = command.getName();
        ArrayList<String> a = command.getArguments();
        if (name.equals("render")) {
            requireCount(command, 0, "render");
            return rendering.render(engine.getDocument());
        }
        if (name.equals("stats")) {
            requireCount(command, 0, "stats");
            // Count directly here too: a macro's engine notification happens
            // only after the whole replay, but a stats step must see current edits.
            DocumentStatistics current = new DocumentStatistics(engine.getDocument());
            return "Words: " + current.getWordCount()
                    + "\nCharacters: " + current.getCharacterCount()
                    + "\nParagraphs: " + current.getParagraphCount();
        }
        if (name.equals("width")) {
            requireCount(command, 1, "width number");
            rendering.setWidth(number(a.get(0)));
            return "Width changed to " + a.get(0) + ".";
        }
        if (name.equals("view")) {
            requireCount(command, 1, "view plain|marked");
            String view = a.get(0).toLowerCase(Locale.ROOT);
            if (view.equals("plain")) {
                rendering.setRenderer(new PlainTextRenderer());
            } else if (view.equals("marked")) {
                rendering.setRenderer(new MarkedUpRenderer());
            } else {
                throw new IllegalArgumentException("Choose plain or marked.");
            }
            return "View changed to " + view + ".";
        }
        if (name.equals("wrap")) {
            requireCount(command, 1, "wrap greedy|balanced");
            String strategy = a.get(0).toLowerCase(Locale.ROOT);
            if (strategy.equals("greedy")) {
                rendering.setLineBreaker(new GreedyLineBreaker());
            } else if (strategy.equals("balanced")) {
                rendering.setLineBreaker(new BalancedLineBreaker());
            } else {
                throw new IllegalArgumentException("Choose greedy or balanced.");
            }
            return "Wrapping changed to " + strategy + ".";
        }
        if (name.equals("checkpoint")) {
            requireRange(command, 1, 2, "checkpoint name [overwrite]");
            if (a.size() == 2 && !a.get(1).equalsIgnoreCase("overwrite")) {
                throw new IllegalArgumentException("Use overwrite to replace a checkpoint.");
            }
            if (checkpoints.hasCheckpoint(a.get(0)) && a.size() == 1) {
                throw new IllegalArgumentException("Checkpoint exists. Use checkpoint name overwrite.");
            }
            checkpoints.createCheckpoint(a.get(0), engine.getDocument());
            return "Checkpoint saved: " + a.get(0);
        }
        if (name.equals("checkpoints")) {
            requireCount(command, 0, "checkpoints");
            return String.join("\n", checkpoints.getCheckpointNames());
        }
        if (name.equals("find")) {
            requireRange(command, 1, 2, "find \"Query\" [substring|word]");
            MatchMode mode = MatchMode.SUBSTRING;
            if (a.size() == 2) {
                mode = matchMode(a.get(1));
            }
            List<SearchMatch> matches = search.find(engine.getDocument(), a.get(0), mode);
            if (matches.isEmpty()) {
                return "No matches found.";
            }
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < matches.size(); i++) {
                SearchMatch match = matches.get(i);
                if (i > 0) {
                    output.append("\n");
                }
                output.append(i + 1).append(". ");
                output.append(location(match.getTarget()));
                output.append(" [").append(match.getStart()).append(", ");
                output.append(match.getEnd()).append(")");
            }
            return output.toString();
        }
        if (name.equals("help")) {
            requireCount(command, 0, "help");
            return "section \"Title\"\nsubsection parentPath \"Title\"\n"
                    + "paragraph sectionPath \"Text\"\n"
                    + "insert sectionPath paragraph position \"Text\"\n"
                    + "delete sectionPath paragraph start end\n"
                    + "replace sectionPath paragraph start end \"Text\"\n"
                    + "move sourceSection sourceParagraph start end destinationSection destinationParagraph position\n"
                    + "split sectionPath paragraph position\nmerge sectionPath paragraph\n"
                    + "format sectionPath paragraph start end attribute value\n"
                    + "titleformat sectionPath start end attribute value\n"
                    + "view plain|marked\nwrap greedy|balanced\nwidth number\nrender\nstats\n"
                    + "find \"Query\" [substring|word]\n"
                    + "replacefirst \"Query\" \"Replacement\" [substring|word]\n"
                    + "replaceall \"Query\" \"Replacement\" [substring|word]\n"
                    + "checkpoint name [overwrite]\ncheckpoints\nrestore name\n"
                    + "record name\nstop\nreplay name\nundo\nredo\nscript \"Path\"\nexit\n"
                    + "Numbers start at 1; text ranges are zero-based [start, end).\n"
                    + "Attributes: bold, italic, fontName, fontSize, color.";
        }
        throw new IllegalArgumentException("Unknown command: " + name);
    }

    String editMessage(String name) {
        if (name.equals("section")) {
            return "Section created.";
        }
        if (name.equals("subsection")) {
            return "Subsection created.";
        }
        if (name.equals("paragraph")) {
            return "Paragraph created.";
        }
        return "Completed: " + name;
    }

    RenderService rendering() {
        return rendering;
    }

    CheckpointManager checkpoints() {
        return checkpoints;
    }

    private int number(String value) {
        return Integer.parseInt(value);
    }

    private MatchMode matchMode(String value) {
        if (value.equalsIgnoreCase("substring")) {
            return MatchMode.SUBSTRING;
        }
        if (value.equalsIgnoreCase("word") || value.equalsIgnoreCase("whole")) {
            return MatchMode.WHOLE_WORD;
        }
        throw new IllegalArgumentException("Match mode must be substring or word.");
    }

    private void requireCount(ParsedCommand command, int count, String usage) {
        requireRange(command, count, count, usage);
    }

    private void requireRange(ParsedCommand command, int min, int max, String usage) {
        int count = command.getArguments().size();
        if (count < min || count > max) {
            throw new IllegalArgumentException("Usage: " + usage);
        }
    }

    private Section findSection(String path) {
        String[] numbers = path.split("\\.", -1);
        int first = number(numbers[0]);
        List<Section> sections = engine.getDocument().getSections();
        if (first < 1 || first > sections.size()) {
            throw new IllegalArgumentException("Section number does not exist.");
        }
        Section section = sections.get(first - 1);
        for (int i = 1; i < numbers.length; i++) {
            int wanted = number(numbers[i]);
            if (wanted < 1) {
                throw new IllegalArgumentException("Section numbers must be positive.");
            }
            Section selected = null;
            int count = 0;
            List<DocumentNode> children = section.getChildren();
            for (int j = 0; j < children.size(); j++) {
                DocumentNode child = children.get(j);
                if (child.getClass() == Section.class) {
                    count++;
                    if (count == wanted) {
                        selected = (Section) child;
                        break;
                    }
                }
            }
            if (selected == null) {
                throw new IllegalArgumentException("Section path does not exist: " + path);
            }
            section = selected;
        }
        return section;
    }

    private Paragraph findParagraph(String path, int wanted) {
        if (wanted < 1) {
            throw new IllegalArgumentException("Paragraph numbers must be positive.");
        }
        List<DocumentNode> children = findSection(path).getChildren();
        int count = 0;
        for (int i = 0; i < children.size(); i++) {
            DocumentNode child = children.get(i);
            if (child.getClass() == Paragraph.class) {
                count++;
                if (count == wanted) {
                    return (Paragraph) child;
                }
            }
        }
        throw new IllegalArgumentException("Paragraph number does not exist in this section.");
    }

    private String location(FormatTarget target) {
        List<Section> sections = engine.getDocument().getSections();
        for (int i = 0; i < sections.size(); i++) {
            String found = locationInSection(sections.get(i), target, "" + (i + 1));
            if (!found.isEmpty()) {
                return found;
            }
        }
        throw new IllegalStateException("Search target is no longer in the document.");
    }

    private String locationInSection(Section section, FormatTarget target, String path) {
        if (section == target) {
            return "Section " + path + " title";
        }
        int sectionNumber = 0;
        int paragraphNumber = 0;
        List<DocumentNode> children = section.getChildren();
        for (int i = 0; i < children.size(); i++) {
            DocumentNode child = children.get(i);
            if (child.getClass() == Paragraph.class) {
                paragraphNumber++;
                if (child == target) {
                    return "Section " + path + " paragraph " + paragraphNumber;
                }
            } else if (child.getClass() == Section.class) {
                sectionNumber++;
                String found = locationInSection((Section) child, target,
                        path + "." + sectionNumber);
                if (!found.isEmpty()) {
                    return found;
                }
            }
        }
        return "";
    }
}
