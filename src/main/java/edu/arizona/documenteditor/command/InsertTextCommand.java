package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextStyle;

import java.util.Objects;

/**
 * Inserts text into a paragraph.
 */
public final class InsertTextCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int position;
    private final String text;
    private final TextStyle style;

    private boolean executed;

    public InsertTextCommand(
            Paragraph paragraph, int position, String text) {

        this(paragraph, position, text,
                new TextStyle(false, false, "Arial", 12, "black"));
    }

    public InsertTextCommand(
            Paragraph paragraph,
            int position,
            String text,
            TextStyle style) {

        this.paragraph = Objects.requireNonNull(
                paragraph, "Paragraph cannot be null.");

        this.text = Objects.requireNonNull(text, "Text cannot be null.");
        this.style = Objects.requireNonNull(style, "Style cannot be null.");

        if (text.isEmpty()) {
            throw new IllegalArgumentException("Inserted text cannot be empty.");
        }

        this.position = position;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command is already executed.");
        }

        paragraph.insertText(position, text, style);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo an insertion that was not executed.");
        }

        paragraph.deleteText(position, position + text.length());
        executed = false;
    }
}
