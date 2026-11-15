//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    @DisplayName("Can create empty document and order Sections")
    void creatEmptyDocThenOrderSection(){
        Document d = new Document();
        assertTrue(d.getSections().isEmpty());
        assertNull(d.getParent());
        Section a = new Section("A");
        Section b = new Section("B");
        d.addSection(a); d.insertSection(0, b);
        assertEquals(List.of(b, a), d.getSections());
        assertSame(d, a.getParent()); assertThrows(UnsupportedOperationException.class, () -> d.getSections().clear());
    }

    @Test
    @DisplayName("Rejects duplicate ownerships")
    void rejectDuplicateOwnership(){
        Document d = new Document(); Section s = new Section("S");
        d.addSection(s);
        assertThrows(IllegalArgumentException.class, () -> d.addSection(s));
        assertThrows(IllegalArgumentException.class, () -> new Document().addSection(s));
        assertSame(s, d.removeSection(0));
        assertNull(s.getParent());
    }

    @Test
    @DisplayName("getParagraphs travels nested childs in order without getting SectionTitles")
    void getParagraphsInOrderWithoutTitles(){
        Document d = new Document();
        Section a = new Section("A");
        Section b = new Section("B");
        Paragraph one = new Paragraph("1");
        Paragraph two = new Paragraph("2");
        Paragraph three = new Paragraph("3");
        a.addChild(one);
        a.addChild(b);
        b.addChild(two);
        a.addChild(three);
        d.addSection(a);
        assertEquals(List.of(one, two, three), d.getParagraphs());
    }

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
    @DisplayName("Undoing a restoration can revive object referenced by an earlier edit command")
    void restorationAlsoRestoresEarlierCommandTargets(){

    }

}
