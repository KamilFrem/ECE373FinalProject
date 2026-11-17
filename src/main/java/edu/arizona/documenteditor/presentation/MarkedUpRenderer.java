package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.DocumentNode;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import java.util.ArrayList;
import java.util.List;

/** Displays document text with visible formatting markers. */
public final class MarkedUpRenderer implements Renderer {

    /** {@inheritDoc} */
    @Override
    public String render(Document document, LineBreaker lineBreaker, int width) {
        if (document == null) {
            throw new NullPointerException("Document cannot be null.");
        }

        if (lineBreaker == null) {
            throw new NullPointerException("Line breaker cannot be null.");
        }

        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive.");
        }

        StringBuilder output = new StringBuilder();
        List<Section> sections = document.getSections();

        for (int i = 0; i < sections.size(); i++) {
            renderSection(sections.get(i), lineBreaker, width, output);
        }

        if (output.length() > 0) {
            output.setLength(output.length() - 1);
        }

        return output.toString();
    }

    private void renderSection(Section section, LineBreaker lineBreaker, int width, StringBuilder output) {
        renderRuns(section.getRuns(), lineBreaker, width, output);

        List<DocumentNode> children = section.getChildren();

        for (int i = 0; i < children.size(); i++) {
            DocumentNode child = children.get(i);

            if (child.getClass() == Section.class) {
                Section nestedSection = (Section) child;
                renderSection(nestedSection, lineBreaker, width, output);
            }

            if (child.getClass() == Paragraph.class) {
                Paragraph paragraph = (Paragraph) child;
                renderRuns(paragraph.getRuns(), lineBreaker, width, output);
            }
        }
    }

    private void renderRuns(List<TextRun> runs, LineBreaker lineBreaker,
                            int width, StringBuilder output) {
        StringBuilder text = new StringBuilder();
        List<TextStyle> styles = new ArrayList<>();

        // Combine the runs and remember the style of each character.
        for (int i = 0; i < runs.size(); i++) {
            TextRun run = runs.get(i);
            String runText = run.getText();

            for (int j = 0; j < runText.length(); j++) {
                text.append(runText.charAt(j));
                styles.add(run.getStyle());
            }
        }

        StringBuilder cleanText = new StringBuilder();
        List<TextStyle> cleanStyles = new ArrayList<>();

        // Match the whitespace normalization used by our line breakers.
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (Character.isWhitespace(character)) {
                if (cleanText.length() > 0) {
                    int last = cleanText.length() - 1;

                    if (cleanText.charAt(last) != ' ') {
                        cleanText.append(' ');
                        cleanStyles.add(styles.get(i));
                    }
                }
            } else {
                cleanText.append(character);
                cleanStyles.add(styles.get(i));
            }
        }

        // Remove a trailing space, if one remains.
        if (cleanText.length() > 0) {
            int last = cleanText.length() - 1;

            if (cleanText.charAt(last) == ' ') {
                cleanText.setLength(last);
                cleanStyles.remove(last);
            }
        }

        List<String> lines = lineBreaker.breakLines(cleanText.toString(), width);
        int position = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int index = 0;

            while (index < line.length()) {
                TextStyle style = cleanStyles.get(position + index);
                StringBuilder piece = new StringBuilder();

                // Collect consecutive characters with the same style.
                while (index < line.length()) {
                    TextStyle currentStyle = cleanStyles.get(position + index);

                    if (!style.equals(currentStyle)) {
                        break;
                    }

                    piece.append(line.charAt(index));
                    index++;
                }

                appendStyledText(piece.toString(), style, output);
            }

            output.append("\n");
            position = position + line.length();

            // A line break replaces the space between the two lines.
            if (i < lines.size() - 1) {
                position++;
            }
        }

        if (lines.isEmpty()) {
            output.append("\n");
        }
    }

    private void appendStyledText(String text, TextStyle style,
                                  StringBuilder output) {
        output.append("[font=");
        output.append(style.getFontName());
        output.append("]");

        output.append("[size=");
        output.append(style.getFontSize());
        output.append("]");

        output.append("[color=");
        output.append(style.getColor());
        output.append("]");

        if (style.isBold()) {
            output.append("[bold]");
        }

        if (style.isItalic()) {
            output.append("[italic]");
        }

        output.append(text);

        // Close markers in the reverse order that they were opened.
        if (style.isItalic()) {
            output.append("[/italic]");
        }

        if (style.isBold()) {
            output.append("[/bold]");
        }

        output.append("[/color][/size][/font]");
    }
}
