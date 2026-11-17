package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import java.util.List;
import java.util.Objects;

/**
 * Replaces a selected range of paragraph text.
 */
public final class ReplaceTextCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int start;
    private final int end;
    private final String replacement;
    private final TextStyle replacementStyle;

    private List<TextRun> originalRuns;
    private boolean executed;

    public ReplaceTextCommand(
            Paragraph paragraph,
            int start,
            int end,
            String replacement) {

        this(paragraph, start, end, replacement,
                new TextStyle(false, false, "Arial", 12, "black"));
    }

    public ReplaceTextCommand(
            Paragraph paragraph,
            int start,
            int end,
            String replacement,
            TextStyle replacementStyle) {

        this.paragraph = Objects.requireNonNull(
                paragraph, "Paragraph cannot be null.");

        this.replacement = Objects.requireNonNull(
                replacement, "Replacement cannot be null.");

        this.replacementStyle = Objects.requireNonNull(
                replacementStyle, "Style cannot be null.");

        this.start = start;
        this.end = end;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command is already executed.");
        }

        List<TextRun> previous = paragraph.slice(start, end);

        if (replacement.isEmpty()) {
            paragraph.deleteText(start, end);
        } else {
            paragraph.replaceText(
                    start, end, replacement, replacementStyle);
        }

        originalRuns = previous;
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a replacement that was not executed.");
        }

        int replacementEnd = start + replacement.length();

        if (replacement.isEmpty()) {
            paragraph.insertRuns(start, originalRuns);
        } else {
            paragraph.replaceRuns(start, replacementEnd, originalRuns);
        }

        executed = false;
    }
}
