package edu.arizona.documenteditor.model;

/**
 * DocumentNode is a node with at most one parent in the document hierarchy.
 * Copies preserve content while detaching the copied part from its old parent.
 */
public interface DocumentNode {
    /**
     * Returns this node's text.
     * @return the text inside this node
     */
    String getText();

    /**
     * Returns the container that owns this node.
     * @return the parent, or {@code null} for a root or detached node
     */
    DocumentNode getParent();

    /**
     * Copies all mutable content without retaining the original parent.
     * @return an independent copy.
     */
    DocumentNode deepCopy();
}
