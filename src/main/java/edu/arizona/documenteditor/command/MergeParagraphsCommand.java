package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;

import java.util.List;
import java.util.Objects;

/**
 * Merges two adjacent paragraphs into the first paragraph.
 */
public final class MergeParagraphsCommand implements EditCommand {

    private final Section parent;
    private final Paragraph first;
    private final Paragraph second;

    private List<TextRun> originalFirstRuns;
    private int secondIndex;
    private boolean executed;

    public MergeParagraphsCommand(
            Section parent,
            Paragraph first,
            Paragraph second) {

        this.parent = Objects.requireNonNull(parent, "Parent cannot be null.");
        this.first = Objects.requireNonNull(first, "First paragraph cannot be null.");
        this.second = Objects.requireNonNull(second, "Second paragraph cannot be null.");

        if (first == second) {
            throw new IllegalArgumentException(
                    "Cannot merge a paragraph with itself.");
        }
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command is already executed.");
        }

        List<edu.arizona.documenteditor.model.DocumentNode> children =
                parent.getChildren();

        int firstIndex = children.indexOf(first);
        secondIndex = children.indexOf(second);

        if (firstIndex < 0 || secondIndex != firstIndex + 1) {
            throw new IllegalArgumentException(
                    "Paragraphs must be adjacent children of the same section.");
        }

        originalFirstRuns = first.getRuns();

        // Paragraph.merge preserves the second paragraph's text styles.
        first.merge(second);

        try {
            parent.removeChild(second);
        } catch (RuntimeException exception) {
            first.setRuns(originalFirstRuns);
            throw exception;
        }

        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a merge that was not executed.");
        }

        parent.insertChild(secondIndex, second);
        first.setRuns(originalFirstRuns);

        executed = false;
    }
}
