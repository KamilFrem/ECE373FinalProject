package edu.arizona.documenteditor.formatting;

import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.model.FormatTarget;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import java.util.ArrayList;
import java.util.List;

/** Changes one formatting attribute in a paragraph or section title. */
public final class FormatCommand implements EditCommand {
    private FormatTarget target;
    private int start;
    private int end;
    private String attribute;
    private String value;
    private List<TextRun> previousRuns;

    /**
     * Creates a formatting command.
     *
     * @param target the paragraph or section title
     * @param start the first selected character
     * @param end the position after the last selected character
     * @param attribute bold, italic, fontName, fontSize, or color
     * @param value the new value for the attribute
     */
    public FormatCommand(FormatTarget target, int start, int end, String attribute, String value) {
        if (target == null) {
            throw new NullPointerException("Target cannot be null.");
        }

        if (attribute == null || value == null) {
            throw new NullPointerException("Attribute and value cannot be null.");
        }

        this.target = target;
        this.start = start;
        this.end = end;
        this.attribute = attribute;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (start < 0 || end > target.length() || start >= end) {
            throw new IllegalArgumentException("Invalid formatting range.");
        }

        List<TextRun> oldRuns = new ArrayList<>(target.getRuns());
        List<TextRun> newRuns = new ArrayList<>();
        int position = 0;

        for (int i = 0; i < oldRuns.size(); i++) {
            TextRun run = oldRuns.get(i);
            String text = run.getText();
            TextStyle style = run.getStyle();

            int runStart = position;
            int runEnd = position + text.length();

            if (runEnd <= start || runStart >= end) {
                // This run is outside the selected range.
                newRuns.add(run);
            } else {
                int selectedStart = start - runStart;
                int selectedEnd = end - runStart;

                if (selectedStart < 0) {
                    selectedStart = 0;
                }

                if (selectedEnd > text.length()) {
                    selectedEnd = text.length();
                }

                // Keep the part before the selection.
                if (selectedStart > 0) {
                    String before = text.substring(0, selectedStart);
                    newRuns.add(new TextRun(before, style));
                }

                // Change only the selected part.
                String selected = text.substring(selectedStart, selectedEnd);
                TextStyle newStyle = changeStyle(style);
                newRuns.add(new TextRun(selected, newStyle));

                // Keep the part after the selection.
                if (selectedEnd < text.length()) {
                    String after = text.substring(selectedEnd);
                    newRuns.add(new TextRun(after, style));
                }
            }

            position = runEnd;
        }

        // Apply only after the entire replacement has been prepared.
        target.setRuns(newRuns);
        previousRuns = oldRuns;
    }

    /** {@inheritDoc} */
    @Override
    public void undo() {
        if (previousRuns == null) {
            throw new IllegalStateException(
                    "Execute the command before undoing it.");
        }

        target.setRuns(previousRuns);
    }

    private TextStyle changeStyle(TextStyle style) {
        if (attribute.equals("bold") || attribute.equals("italic")) {
            if (!value.equals("true") && !value.equals("false")) {
                throw new IllegalArgumentException("Bold and italic require true or false.");
            }

            boolean enabled = value.equals("true");

            if (attribute.equals("bold")) {
                return style.withBold(enabled);
            }

            return style.withItalic(enabled);
        }

        if (attribute.equals("fontName")) {
            return style.withFontName(value);
        }

        if (attribute.equals("fontSize")) {
            int size = Integer.parseInt(value);
            return style.withFontSize(size);
        }

        if (attribute.equals("color")) {
            return style.withColor(value);
        }

        throw new IllegalArgumentException("Unknown formatting attribute.");
    }
}
