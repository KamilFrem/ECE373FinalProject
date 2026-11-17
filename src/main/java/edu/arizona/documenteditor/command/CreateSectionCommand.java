package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.DocumentNode;
import edu.arizona.documenteditor.model.Section;

import java.util.Objects;

/**
 * Creates a section in a document or inside another section.
 */
public final class CreateSectionCommand implements EditCommand {

    private final DocumentNode parent;
    private final int index;
    private final Section section;
    private boolean executed;

    public CreateSectionCommand(
            DocumentNode parent, int index, String title) {

        this.parent = Objects.requireNonNull(parent, "Parent cannot be null.");

        if (!(parent instanceof Document) && !(parent instanceof Section)) {
            throw new IllegalArgumentException(
                    "Parent must be a Document or Section.");
        }

        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        this.index = index;
        this.section = new Section(title);
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Section already created.");
        }

        if (parent instanceof Document document) {
            document.insertSection(index, section);
        } else {
            ((Section) parent).insertChild(index, section);
        }

        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException(
                    "Cannot undo a section that was not created.");
        }

        if (parent instanceof Document document) {
            document.removeSection(section);
        } else {
            ((Section) parent).removeChild(section);
        }

        executed = false;
    }

    public Section getSection() {
        return section;
    }
}
