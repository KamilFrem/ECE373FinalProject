package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.command.ReplaceTextCommand;
import edu.arizona.documenteditor.model.FormatTarget;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import java.util.ArrayList;
import java.util.List;

/** Replaces a search match in a paragraph or section title. */
public final class ReplaceMatchCommand implements EditCommand {

    private final FormatTarget target;
    private final int start;
    private final int end;
    private final String replacement;

    private List<TextRun> previousRuns;
    private List<TextRun> replacementRuns;
    private boolean executed;

    /**
     * Creates a replacement for one search result.
     *
     * @param match the result to replace
     * @param replacement the new text; may be empty
     * @throws NullPointerException if either argument is null
     */
    public ReplaceMatchCommand(
            SearchMatch match, String replacement) {

        if (match == null || replacement == null) {
            throw new NullPointerException(
                    "Match and replacement cannot be null.");
        }

        target = match.getTarget();
        start = match.getStart();
        end = match.getEnd();
        this.replacement = replacement;
    }

    /**
     * Replaces the match or reapplies an undone replacement.
     * New text uses the style of the first matched character.
     *
     * @throws IllegalStateException if already executed
     * @throws IndexOutOfBoundsException if the range is invalid
     */
    @Override
    public void execute() {
        if (executed) {

            throw new IllegalStateException("Replacement is already executed.");

        }

        if (replacementRuns == null) {
            // Prepare the change without modifying the actual target.
            Paragraph working = new Paragraph(target.getRuns());

            List<TextRun> selected = working.slice(start, end);

            TextStyle style = selected.get(0).getStyle();

            ReplaceTextCommand command = new ReplaceTextCommand(working, start, end, replacement, style);

            command.execute();

            previousRuns = new ArrayList<TextRun>(target.getRuns());

            replacementRuns = new ArrayList<TextRun>(working.getRuns());

        }

        target.setRuns(replacementRuns);
        executed = true;
    }

    /**
     * Restores the original text and formatting.
     *
     * @throws IllegalStateException if not currently executed
     */
    @Override
    public void undo() {
        if (executed == false) {

            throw new IllegalStateException("Execute the replacement before undoing it.");

        }

        target.setRuns(previousRuns);
        executed = false;
    }
}