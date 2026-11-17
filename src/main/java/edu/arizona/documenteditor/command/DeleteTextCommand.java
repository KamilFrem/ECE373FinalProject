package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextRun;

import java.util.List;
import java.util.Objects;

/**
 * Deletes a selected range from a paragraph.
 */
public final class DeleteTextCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int start;
    private final int end;

    private List<TextRun> deletedRuns;
    private boolean executed;

    public DeleteTextCommand(
            Paragraph paragraph, int start, int end) {

        this.paragraph = Objects.requireNonNull(
                paragraph, "Paragraph cannot be null.");

        this.start = start;
        this.end = end;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command is already executed.");
        }

        // Save the original text and formatting before deletion.
        List<TextRun> original = paragraph.slice(start, end);

        paragraph.deleteText(start, end);

        deletedRuns = original;
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a deletion that was not executed.");
        }

        paragraph.insertRuns(start, deletedRuns);
        executed = false;
    }
}
