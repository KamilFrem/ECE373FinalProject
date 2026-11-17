package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;

import java.util.List;
import java.util.Objects;

/**
 * Creates a paragraph inside a section.
 */
public final class CreateParagraphCommand implements EditCommand {

    private final Section parent;
    private final int index;
    private final Paragraph paragraph;
    private boolean executed;

    public CreateParagraphCommand(
            Section parent, int index, String text) {

        this.parent = Objects.requireNonNull(parent, "Parent cannot be null.");

        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        this.index = index;
        this.paragraph = new Paragraph(text);
    }

    public CreateParagraphCommand(
            Section parent, int index, List<TextRun> runs) {

        this.parent = Objects.requireNonNull(parent, "Parent cannot be null.");

        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        this.index = index;
        this.paragraph = new Paragraph(runs);
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Paragraph already created.");
        }

        parent.insertChild(index, paragraph);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a paragraph that was not created.");
        }

        parent.removeChild(paragraph);
        executed = false;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }
}