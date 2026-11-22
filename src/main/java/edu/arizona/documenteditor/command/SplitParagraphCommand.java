package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;

import java.util.List;
import java.util.Objects;

/**
 * Splits a paragraph at a specified character position.
 */
public final class SplitParagraphCommand implements EditCommand {

    private final Paragraph paragraph;
    private final int position;

    private Section parent;
    private int childIndex;
    private Paragraph secondParagraph;
    private List<TextRun> originalRuns;
    private boolean executed;

    /**
     * Creates a split operation.
     * @param paragraph paragraph to split
     * @param position zero-based split position
     * @throws NullPointerException if paragraph is null
     */
    public SplitParagraphCommand(Paragraph paragraph, int position) {
        this.paragraph = Objects.requireNonNull(
                paragraph, "Paragraph cannot be null.");

        this.position = position;
    }

    /** Executes the split or reapplies it after undo. */
    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command is already executed.");
        }

        parent = paragraph.getParent();

        if (parent == null) {
            throw new IllegalArgumentException(
                    "Paragraph must belong to a section.");
        }

        originalRuns = paragraph.getRuns();

        childIndex = parent.getChildren().indexOf(paragraph);

        if (childIndex < 0) {
            throw new IllegalStateException(
                    "Paragraph is not present in its parent.");
        }

        // split() returns the later half as a detached paragraph.
        Paragraph suffix = paragraph.split(position);
        // Reuse the same suffix object on redo. Later commands may hold it.
        if (secondParagraph == null) {
            secondParagraph = suffix;
        } else {
            secondParagraph.setRuns(suffix.getRuns());
        }

        try {
            parent.insertChild(childIndex + 1, secondParagraph);
        } catch (RuntimeException exception) {
            paragraph.setRuns(originalRuns);
            throw exception;
        }

        executed = true;
    }

    /** Restores the original paragraph and removes the suffix. */
    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a split that was not executed.");
        }

        parent.removeChild(secondParagraph);
        paragraph.setRuns(originalRuns);

        executed = false;
    }

    /** @return the suffix paragraph, or null before the first execution */
    public Paragraph getSecondParagraph() {
        return secondParagraph;
    }
}