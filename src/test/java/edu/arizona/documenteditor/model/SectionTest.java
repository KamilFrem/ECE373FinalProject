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
    void regularInterfaceCannotBypassSupportedChildValidation(){
        DocumentNode customNode = new DocumentNode() {
            @Override
            public String getText() { return "custom"; }
            @Override
            public DocumentNode getParent() { return null; }
            @Override
            public DocumentNode deepCopy() { return this; }
        };
        Section section = new Section("Title");
        Paragraph existing = new Paragraph("body");
        section.addChild(existing);
        assertThrows(IllegalArgumentException.class, () -> section.addChild(customNode));
        assertEquals(List.of(existing), section.getChildren());
        assertSame(section, existing.getParent());
        assertNull(customNode.getParent());
    }

    @Test
    @DisplayName("Object based child removal supports undo")
    void objectBasedChildRemovalSupportsUndo(){
        Section section = new Section("S");
        Paragraph first = new Paragraph("Same text");
        Paragraph second = new Paragraph("Same text");
        section.addChild(first);
        section.addChild(second);
        section.removeChild(first);
        assertEquals(List.of(second), section.getChildren());
        assertNull(first.getParent());
        section.insertChild(0, first);
        assertEquals(List.of(first, second), section.getChildren());
        assertSame(section, first.getParent());
    }

    @Test
    @DisplayName("Trying to remove non-existing child does not change section structure")
    void removingAbsentChildDoesNotChangeStructure(){
        Section section = new Section("S");
        Paragraph existing = new Paragraph("body");
        section.addChild(existing);
        assertThrows(IllegalArgumentException.class, () -> section.removeChild(new Paragraph("body")));
        assertThrows(NullPointerException.class, () -> section.removeChild((DocumentNode) null));
        assertEquals(List.of(existing), section.getChildren());
        assertSame(section, existing.getParent());
    }

    @Test
    @DisplayName("Setting empty title is allowed")
    void emptyTitleRemainsValid(){
        Section section = new Section("");
        section.setTitle("New");
        assertEquals("New", section.getText());
        section.setTitle("");
        assertEquals("", section.getText());
    }

    @Test
    @DisplayName("Invalid title change request leaves text and styles unchanged")
    void invalidTitleChangeLeavesTextAndStyleUnchanged(){
        FormatTarget target = new Section("Title");
        List<TextRun> before = target.getRuns();
        assertThrows(NullPointerException.class, () -> target.setRuns(java.util.Arrays.asList(new TextRun("x", DEFAULT_STYLE), null)));
        assertEquals(before, target.getRuns());
    }

    @Test
    @DisplayName("A command can undo split by removing its new child and restoring saved runs")
    void splitSupportsUndoAndRedoWithSameChild(){
        Section section = new Section("Title");
        Paragraph first = new Paragraph("abcdef", bold);
        section.addChild(first);
        List<TextRun> original = first.getRuns();
        int index = section.getChildren().indexOf(first);
        Paragraph suffix = first.split(3);
        List<TextRun> prefix = first.getRuns();
        section.insertChild(index + 1, suffix);
        assertEquals("abc", first.getText());
        assertEquals("def", suffix.getText());
        section.removeChild(suffix);
        first.setRuns(original);
        assertEquals(List.of(first), section.getChildren());
        assertNull(suffix.getParent());
        first.setRuns(prefix);
        section.insertChild(index + 1, suffix);
        assertSame(suffix, section.getChildren().get(1));
        assertEquals("abcdef", first.getText() + suffix.getText());
    }

    @Test
    @DisplayName("Merge method supports undo and redo without replacing nodes")
    void mergeSupportUndoRedoWithoutReplacingNode(){
        Section section = new Section("Title");
        Paragraph first = new Paragraph("abc");
        Paragraph second = new Paragraph("def", DEFAULT_STYLE.withItalic(true));
        section.addChild(first);
        section.addChild(second);
        List<TextRun> before = first.getRuns();
        int index = section.getChildren().indexOf(second);
        first.merge(second);
        section.removeChild(second);
        assertEquals("abcdef", first.getText());
        assertNull(second.getParent());
        first.setRuns(before);
        section.insertChild(index, second);
        assertEquals("abc", first.getText());
        assertSame(second, section.getChildren().get(index));
        first.merge(second);
        section.removeChild(second);
        assertEquals("abcdef", first.getText());
        assertEquals(List.of(first), section.getChildren());
    }

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
