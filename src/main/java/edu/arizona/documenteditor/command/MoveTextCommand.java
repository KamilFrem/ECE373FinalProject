package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextRun;

import java.util.List;
import java.util.Objects;

/**
 * Moves a styled text range between paragraphs or within one paragraph.
 */
public final class MoveTextCommand implements EditCommand {

    private final Paragraph source;
    private final Paragraph destination;
    private final int start;
    private final int end;
    private final int destinationPosition;

    private List<TextRun> movedRuns;
    private int adjustedDestination;
    private boolean executed;

    public MoveTextCommand(
            Paragraph source,
            int start,
            int end,
            Paragraph destination,
            int destinationPosition) {

        this.source = Objects.requireNonNull(source, "Source cannot be null.");
        this.destination = Objects.requireNonNull(
                destination, "Destination cannot be null.");

        this.start = start;
        this.end = end;
        this.destinationPosition = destinationPosition;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Command is already executed.");
        }

        // Validate and capture the selected styled text before changing anything.
        List<TextRun> selected = source.slice(start, end);

        if (destinationPosition < 0
                || destinationPosition > destination.length()) {
            throw new IndexOutOfBoundsException(
                    "Destination position is outside the paragraph.");
        }

        if (source == destination) {
            if (destinationPosition >= start
                    && destinationPosition <= end) {
                throw new IllegalArgumentException(
                        "Destination cannot be inside the moved range.");
            }

            adjustedDestination = destinationPosition > end
                    ? destinationPosition - (end - start)
                    : destinationPosition;

            List<TextRun> original = source.getRuns();

            try {
                source.deleteText(start, end);
                source.insertRuns(adjustedDestination, selected);
            } catch (RuntimeException exception) {
                source.setRuns(original);
                throw exception;
            }
        } else {
            List<TextRun> originalSource = source.getRuns();
            List<TextRun> originalDestination = destination.getRuns();

            try {
                source.deleteText(start, end);
                destination.insertRuns(destinationPosition, selected);
            } catch (RuntimeException exception) {
                source.setRuns(originalSource);
                destination.setRuns(originalDestination);
                throw exception;
            }

            adjustedDestination = destinationPosition;
        }

        movedRuns = selected;
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a move that was not executed.");
        }

        int movedLength = end - start;

        if (source == destination) {
            List<TextRun> original = source.getRuns();

            try {
                source.deleteText(
                        adjustedDestination,
                        adjustedDestination + movedLength);

                source.insertRuns(start, movedRuns);
            } catch (RuntimeException exception) {
                source.setRuns(original);
                throw exception;
            }
        } else {
            List<TextRun> originalSource = source.getRuns();
            List<TextRun> originalDestination = destination.getRuns();

            try {
                destination.deleteText(
                        destinationPosition,
                        destinationPosition + movedLength);

                source.insertRuns(start, movedRuns);
            } catch (RuntimeException exception) {
                source.setRuns(originalSource);
                destination.setRuns(originalDestination);
                throw exception;
            }
        }

        executed = false;
    }
}
