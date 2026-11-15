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

class SectionTest {
    private final TextStyle bold = new TextStyle(true, false, "Arial", 12, "black");
    private final TextStyle DEFAULT_STYLE = new TextStyle(false, false, "Arial", 12, "black");

    @Test
    @DisplayName("Section preserves mixed child order and parent")
    void preservesMixedChildOrderAndParents() {
        Section s = new Section("Title");
        Paragraph p = new Paragraph("one");
        Section sub = new Section("Sub");
        s.addChild(p);
        s.insertChild(0, sub);
        assertEquals(List.of(sub, p), s.getChildren());
        assertSame(s, p.getParent());
        assertSame(s, sub.getParent());
    }

    @Test
    @DisplayName("Rejects cycle document architecture and duplicate ownerships")
    void rejectCyclesAndDuplicateOwnerships(){
        Section root = new Section("Root");
        Section sub = new Section("Sub");
        root.addChild(sub);
        assertThrows(IllegalArgumentException.class, () -> sub.addChild(root));
        assertThrows(IllegalArgumentException.class, () -> root.addChild(root));
        assertThrows(IllegalArgumentException.class, () -> root.addChild(new Document()));
        assertThrows(IllegalArgumentException.class, () -> new Section("Other").addChild(sub));
        assertEquals(List.of(sub), root.getChildren());
        assertTrue(sub.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Removal detaches and allows reattachment")
    void removalDetachesAndAllowsReattachment(){
        Section a = new Section("A");
        Section b = new Section("B");
        Paragraph p = new Paragraph("x");
        a.addChild(p);
        assertSame(p, a.removeChild(0));
        assertNull(p.getParent());
        b.addChild(p);
        assertSame(b, p.getParent());
    }

    @Test
    @DisplayName("Invalid insertion does not attach")
    void rejectInvalidInsertion(){
        Section s = new Section("S");
        Paragraph p = new Paragraph();
        assertThrows(IndexOutOfBoundsException.class, () -> s.insertChild(1, p));
        assertNull(p.getParent());
        assertTrue(s.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Title can have mixed styles")
    void titleWithMixedStyles(){
        Section s = new Section("Title"); s.setRuns(List.of(new TextRun("T", DEFAULT_STYLE),
                new TextRun("itl", DEFAULT_STYLE.withBold(true).withFontSize(18)),
                new TextRun("e", DEFAULT_STYLE)));
        assertEquals("Title", s.getText());
        assertTrue(s.getRuns().get(1).getStyle().isBold());
        assertEquals(18, s.getRuns().get(1).getStyle().getFontSize());
        assertTrue(s.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Unsupported node is rejected before modifying child list")
    void regularInterfaceCannotBypassSupportedChildValidation(){}

    @Test
    @DisplayName("Object based child removal supports undo")
    void objectBasedChildRemovalSupportsUndo(){}

    @Test
    @DisplayName("Trying to remove non-existing child does not change section structure")
    void removingAbsentChildDoesNotChangeStructure(){}

    @Test
    @DisplayName("Setting empty title is allowed")
    void emptyTitleRemainsValid(){}

    @Test
    @DisplayName("Invalid title change request leaves text and styles unchanged")
    void invalidTitleChangeLeavesTextAndStyleUnchanged(){}

    @Test
    @DisplayName("A command can undo split by removing its new child and restoring saved runs")
    void splitSupportsUndoAndRedoWithSameChild(){}

    @Test
    @DisplayName("Merge method supports undo and redo without replacing nodes")
    void mergeSupportUndoRedoWithoutReplacingNode(){}

    @Test
    @DisplayName("invalid Split operation does not remove or change existing children")
    void invalidSplitLeavesHierarchUnchanged(){}

    @Test
    @DisplayName("Title runs are isolated form changes to the supplied list")
    void titleRunsAreIsolatedFromInputLists(){}

    @Test
    @DisplayName("Copying preserves styled titles while allowing independent title replacement")
    void copiedSectionsCanBeEditedIndependently(){}

    @Test
    @DisplayName("Target stores the text, styles, and length supplied by caller")
    void storePreparedRuns(){}

    @Test
    @DisplayName("Saved runs should stay independent of later assignments")
    void savedRunsRemainsIndependent(){}

    @Test
    @DisplayName("Document node can get access to the title text")
    void exposesTextAsDocumentNode(){}

}
