//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A section can own ordered mixture of paragraphs and nested sections.
 * Its formatting methods such as getText() and setRuns() only for title editing only.
 * Use Paragraph for further editing. Traverse getChlideren() to inspect body content.
 * Structural operations enforce ownership and prevent cycles.
 * This class does not store command history so use command classes to manage history, redo, undo
 */
public final class Section implements DocumentNode, FormatTarget {
    /** Owning document or section, will store null if no owner exists(detached). */
    DocumentNode parent;

    /** Text and style storage for section title. */
    private List<TextRun> title =List.of();
    /** Total Title length. */
    private int titleLength;
    /** Direct paragraphs and subsections in document order. */
    private final List<DocumentNode> children = new ArrayList<>();
    /** Default title style */
    private final TextStyle DEFAULT_STYLE = new TextStyle(true, false, "Arial", 16, "black");

    /**
     * Creates a detached section with a default styled title and no children.
     * @param title text for section title
     * @throws NullPointerException if input is null
     */
    public Section(final String title){setTitle(title);}
    /**
     * Creates a detached section with a text run title and no children.
     * @param title text runs with different styles
     * @throws NullPointerException if input is null
     */
    public Section(final List<TextRun> title){setRuns(title);}

    /**
     * Returns the document or section that owns this section
     * @return owner, or {@code null} when detached
     */
    @Override
    public DocumentNode getParent(){return parent;}

    /**
     * Replaces title text using default style
     * @param value replacement text
     */
    public void setTitle(final String value){
        setRuns(List.of(new TextRun(value, DEFAULT_STYLE)));
    }

    /**
     * Returns the stored text without formatting. It is only title
     * @return Title text
     */
    @Override
    public String getText(){
        StringBuilder text = new StringBuilder(titleLength);
        for (TextRun run : title) {
            text.append(run.getText());
        }
        return text.toString();
    }

    /**
     * Returns the title length
     * @return the number of characters in title
     */
    @Override
    public int length(){return titleLength;}

    /**
     * Returns the snapshot of the text runs from title
     * @return the text runs
     */
    @Override
    public List<TextRun> getRuns(){return title;}

    /**
     * Replaces text and styles
     * Empty runs are removed adjacent runs with same styles are merged
     * Title only gets replaced
     * @param values replacement text runs
     * @throws NullPointerException if a input is null.
     * @throws ArithmeticException if the combined text length is greater than integer range
     */
    @Override
    public void setRuns(final List<TextRun> values){
        Objects.requireNonNull(values, "runs");
        List<TextRun> temp = new ArrayList<>();
        int size = 0;
        for (TextRun run : values) {
            Objects.requireNonNull(run, "run");
            size = Math.addExact(size, run.getLength());
            if (run.getLength() == 0) { continue; }
            int last = temp.size() - 1;
            if (last >= 0 && temp.get(last).getStyle().equals(run.getStyle())) {
                TextRun previous = temp.remove(last);
                temp.add(new TextRun(previous.getText() + run.getText(), previous.getStyle()));
            } else {
                temp.add(run);
            }
        }
        title = List.copyOf(temp);
        titleLength = size;
    }

    /**
     * Returns and snapshot og the direct children in document order.
     * @return paragraphs and nested sections in their order
     */
    public List<DocumentNode> getChildren(){return List.copyOf(children);}

    /**
     * Appends detached section or paragraph
     * Ensures that there are no cyclic parent relationship
     * @param child detached child to attach
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if the node is not a section, paragraph, has parent, or creates a cycle
     */
    public void addChild(final DocumentNode child){insertChild(children.size(), child);}

    /**
     * inserts detached section or paragraph
     * Ensures that there are no cyclic parent relationship
     * @param index insertion index from zero through number of children
     * @param child detached child to attach
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException if the node is not a section, paragraph, has parent, or creates a cycle
     * @throws IndexOutOfBoundsException if the insertion index is not within limits of the child list.
     */
    public void insertChild(final int index, final DocumentNode child){
        if (index < 0 || index > children.size()) { throw new IndexOutOfBoundsException("Index cannot be outside of bounds"); }

        Objects.requireNonNull(child, "child that will be added cannot be null");

        if (!(child instanceof Section) && !(child instanceof Paragraph)) {
            throw new IllegalArgumentException("Child must be a paragraph or section");
        }

        if (child.getParent() != null) {
            throw new IllegalArgumentException("Child must be a detached paragraph or section");
        }

        for (DocumentNode ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor == child) { throw new IllegalArgumentException("Cycle in document tree"); }
        }
        children.add(index, child);
        setParent(child, this);
    }

    /**
     * Removes and detaches the child at the supplied index.
     * @param index zero-based child index
     * @return the detached child
     * @throws IndexOutOfBoundsException if the index does not identify a child
     */
    public DocumentNode removeChild(final int index){}

    /**
     * Removes a direct child by object identity and detaches it.
     * @param child child to remove
     * @throws NullPointerException if a required argument is null
     * @throws IllegalArgumentException if the node is not a direct child
     */
    public void removeChild(final DocumentNode child){}

    /**
     * Updates an internal parent link after a validated structural change.
     * @param node section or paragraph whose link is updated
     * @param owner new section owner, or null when detaching
     */
    private static void setParent(final DocumentNode node, final Section owner){}

    /**
     * Copies the title and every descendant into an independent, detached section.
     * Iterative traversal avoids consuming the call stack for deeply nested sections.
     * @return the detached copy with correctly connected descendant parents
     */
    @Override
    public Section deepCopy(){}
}
