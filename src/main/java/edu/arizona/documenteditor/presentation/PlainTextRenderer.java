package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.DocumentNode;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;

import java.util.List;

/** Displays section titles and paragraphs as plain text. */
public final class PlainTextRenderer implements Renderer {

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
            Section section = sections.get(i);
            renderSection(section, lineBreaker, width, output);
        }

        // Remove the extra newline after the final block of text.
        if (output.length() > 0) {
            output.setLength(output.length() - 1);
        }

        return output.toString();
    }

    private void renderSection(Section section, LineBreaker lineBreaker,
                               int width, StringBuilder output) {
        addWrappedText(section.getText(), lineBreaker, width, output);

        List<DocumentNode> children = section.getChildren();

        for (int i = 0; i < children.size(); i++) {
            DocumentNode child = children.get(i);

            if (child.getClass() == Section.class) {
                Section nestedSection = (Section) child;
                renderSection(nestedSection, lineBreaker, width, output);
            }

            if (child.getClass() == Paragraph.class) {
                Paragraph paragraph = (Paragraph) child;
                addWrappedText(paragraph.getText(), lineBreaker, width, output);
            }
        }
    }

    private void addWrappedText(String text, LineBreaker lineBreaker,
                                int width, StringBuilder output) {
        List<String> lines = lineBreaker.breakLines(text, width);

        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                output.append("\n");
            }

            output.append(lines.get(i));
        }

        output.append("\n");
    }
}
