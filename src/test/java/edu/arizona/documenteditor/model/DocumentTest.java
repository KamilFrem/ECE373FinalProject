//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
class DocumentTest {

    @Test
    @DisplayName("Can create empty document and order Sections")
    void creatEmptyDocThenOrderSection(){}

    @Test
    @DisplayName("Rejects duplicate ownerships")
    void rejectDuplicateOwnership(){}

    @Test
    @DisplayName("getParagraphs travels nested childs in order without getting SectionTitles")
    void getParagraphsInOrderWithoutTitles(){}

    @Test
    @DisplayName("Deep Copy copies all mutable content independently")
    void copiesAllContentIndependently(){}

    @Test
    @DisplayName("Restore does not change snapshot and detaches old children")
    void restoreDoesNotChangeSnapshot(){}

    @Test
    @DisplayName("deepCopy and getParagraphs can handle deeply nested document")
    void deeplyNestedTraversal(){}

    @Test
    @DisplayName("getText includes nested content in order")
    void getTextReturnsTextInOrder(){}

    @Test
    @DisplayName("Object based removal only removes requested object")
    void objectBasedRemovalRemovesOnlyRequestedObject(){}

    @Test
    @DisplayName("Removing non-existing objects and null objects are not accepted")
    void nullAndAbsentObjectRemovalRejected(){}

    @Test
    @DisplayName("Copy and restore preserve nested titles and body formatting")
    void copyAndRestorePreserveDocumentStructure(){}

    @Test
    @DisplayName("setSection check all incoming sections before changing the existing roots")
    void invalidSectionReplacementRejected(){}

    @Test
    @DisplayName("Clearing and restoring sections preserves their nested sections and paragraphs")
    void savedSectionListCanUndoSectionRemoval(){}
    
    @Test
    @DisplayName("Undoing a restoration can revive object referenced by an erlier edit command")
    void restorationAlsoRestoresEarlierCommandTargets(){

    }

}
