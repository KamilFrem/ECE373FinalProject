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
    private final TextStyle bold = new TextStyle(true, false, "Arial", 12, "black");
    private final TextStyle DEFAULT_STYLE = new TextStyle(false, false, "Arial", 12, "black");

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
    void copiesAllContentIndependently(){
        Document d = new Document();
        Section s = new Section("Title");
        Section nested = new Section("Nested");
        Paragraph p = new Paragraph("abc");
        nested.addChild(p);
        s.addChild(nested);
        d.addSection(s);
        s.setRuns(List.of(new TextRun("Title", bold)));
        Document copy = d.deepCopy();
        Section cs = copy.getSections().get(0);
        assertNotSame(s, cs);
        assertSame(copy, cs.getParent());
        assertEquals(s.getRuns(), cs.getRuns());
        copy.getParagraphs().get(0).replaceText(0, 3, "changed");
        cs.setTitle("Other");
        assertEquals("abc", p.getText());
        assertEquals("Title", s.getText());
        assertSame(cs, cs.getChildren().get(0).getParent());
    }

    @Test
    @DisplayName("Restore does not change snapshot and detaches old children")
    void restoreDoesNotChangeSnapshot(){
        Document d = new Document();
        Section s = new Section("S");
        s.addChild(new Paragraph("before"));
        d.addSection(s);
        Document snapshot = d.deepCopy();
        d.getParagraphs().get(0).insertText(0, "edit");
        d.restore(snapshot);
        assertNull(s.getParent());
        assertEquals("before", d.getParagraphs().get(0).getText());
        d.getParagraphs().get(0).insertText(0, "new");
        assertEquals("before", snapshot.getParagraphs().get(0).getText());
        d.restore(d);
        assertEquals("newbefore", d.getParagraphs().get(0).getText());
    }

    @Test
    @DisplayName("deepCopy and getParagraphs can handle deeply nested document")
    void deeplyNestedTraversal(){
        Section root = new Section("leaf");
        root.addChild(new Paragraph("deep"));
        for (int i = 0; i < 5000; i++) {
            Section parent = new Section("level");
            parent.addChild(root);
            root = parent;
        }
        Document d = new Document();
        d.addSection(root);
        assertEquals("deep", d.getParagraphs().get(0).getText());
        assertEquals("deep", d.deepCopy().getParagraphs().get(0).getText());
    }

    @Test
    @DisplayName("getText includes nested content in order")
    void getTextReturnsTextInOrder(){
        Document document = new Document();
        Section first = new Section("First");
        Section nested = new Section("Nested");
        Paragraph body = new Paragraph("Body", bold);
        List<TextRun> original = body.getRuns();
        nested.addChild(body);
        first.addChild(nested);
        first.addChild(new Paragraph("Last paragraph"));
        document.addSection(first);
        document.addSection(new Section("Second"));
        assertEquals("First\nNested\nBody\nLast paragraph\nSecond", document.getText());
        assertEquals(original, body.getRuns());
    }

    @Test
    @DisplayName("Object based removal only removes requested object")
    void objectBasedRemovalRemovesOnlyRequestedObject(){
        Document document = new Document();
        Section first = new Section("Same name");
        Section second = new Section("Same name");
        document.addSection(first);
        document.addSection(second);
        document.removeSection(first);
        assertEquals(List.of(second), document.getSections());
        assertNull(first.getParent());
        assertSame(document, second.getParent());
        document.insertSection(0, first);
        assertEquals(List.of(first, second), document.getSections());
    }

    @Test
    @DisplayName("Removing non-existing objects and null objects are not accepted")
    void nullAndAbsentObjectRemovalRejected(){
        Document document = new Document();
        Section top = new Section("Top");
        Section nested = new Section("Nested");
        top.addChild(nested);
        document.addSection(top);
        assertThrows(IllegalArgumentException.class, () -> document.removeSection(nested));
        assertThrows(IllegalArgumentException.class, () -> document.removeSection(new Section("Top")));
        assertThrows(NullPointerException.class, () -> document.removeSection((Section) null));
        assertEquals(List.of(top), document.getSections());
        assertSame(top, nested.getParent());
    }

    @Test
    @DisplayName("Copy and restore preserve nested titles and body formatting")
    void copyAndRestorePreserveDocumentStructure(){
        Document document = new Document();
        Section section = new Section("Title");
        Section nested = new Section("Nested");
        Paragraph body = new Paragraph("Body", DEFAULT_STYLE.withColor("blue"));
        section.setRuns(List.of(new TextRun("Title", bold)));
        nested.setRuns(List.of(new TextRun("Nested", DEFAULT_STYLE.withItalic(true))));
        nested.addChild(body);
        section.addChild(nested);
        document.addSection(section);
        Document snapshot = document.deepCopy();
        section.setTitle("Changed");
        body.deleteText(0, 4);
        document.restore(snapshot);
        Section restored = document.getSections().get(0);
        Section restoredNested = (Section) restored.getChildren().get(0);
        assertEquals("Title\nNested\nBody", document.getText());
        assertTrue(restored.getRuns().get(0).getStyle().isBold());
        assertTrue(restoredNested.getRuns().get(0).getStyle().isItalic());
        assertEquals("blue", document.getParagraphs().get(0).getRuns().get(0).getStyle().getColor());
        restoredNested.setTitle("Changed again");
        assertEquals("Title\nNested\nBody", snapshot.getText());
    }

    @Test
    @DisplayName("setSection check all incoming sections before changing the existing roots")
    void invalidSectionReplacementRejected(){
        Document document = new Document();
        Section current = new Section("Current");
        document.addSection(current);
        Document other = new Document();
        Section foreign = new Section("Foreign");
        other.addSection(foreign);
        Section candidate = new Section("Candidate");
        assertThrows(IllegalArgumentException.class, () -> document.setSections(List.of(candidate, foreign)));
        assertThrows(IllegalArgumentException.class, () -> document.setSections(List.of(current, current)));
        assertThrows(NullPointerException.class, () -> document.setSections(null));
        assertThrows(NullPointerException.class, () -> document.setSections(java.util.Arrays.asList(candidate, null)));
        assertEquals(List.of(current), document.getSections());
        assertSame(document, current.getParent());
        assertSame(other, foreign.getParent());
        assertNull(candidate.getParent());
    }

    @Test
    @DisplayName("Clearing and restoring sections preserves their nested sections and paragraphs")
    void savedSectionListCanUndoSectionRemoval(){
        Document document = new Document();
        Section root = new Section("Root");
        Paragraph paragraph = new Paragraph("Body");
        root.addChild(paragraph);
        document.addSection(root);
        List<Section> saved = document.getSections();
        document.setSections(List.of());
        assertTrue(document.getSections().isEmpty());
        assertNull(root.getParent());
        document.setSections(saved);
        assertSame(root, document.getSections().get(0));
        assertSame(paragraph, document.getParagraphs().get(0));
        assertSame(root, paragraph.getParent());
    }

    @Test
    @DisplayName("Undoing a restoration can revive object referenced by an earlier edit command")
    void restorationAlsoRestoresEarlierCommandTargets(){
        Document document = new Document();
        Section root = new Section("Title");
        Paragraph target = new Paragraph("A");
        root.addChild(target);
        document.addSection(root);
        Document checkpoint = document.deepCopy();
        List<TextRun> beforeEdit = target.getRuns();
        target.insertText(1, "B");
        List<TextRun> afterEdit = target.getRuns();
        List<Section> beforeRestore = document.getSections();
        document.restore(checkpoint);
        List<Section> afterRestore = document.getSections();
        assertEquals("A", document.getParagraphs().get(0).getText());
        assertNotSame(target, document.getParagraphs().get(0));
        document.setSections(beforeRestore);
        assertSame(target, document.getParagraphs().get(0));
        assertEquals("AB", target.getText());
        target.setRuns(beforeEdit);
        assertEquals("A", document.getParagraphs().get(0).getText());
        target.setRuns(afterEdit);
        document.setSections(afterRestore);
        assertEquals("A", document.getParagraphs().get(0).getText());
        document.setSections(beforeRestore);
        assertSame(target, document.getParagraphs().get(0));
        assertEquals("AB", target.getText());
        assertEquals("A", checkpoint.getParagraphs().get(0).getText());
    }

}
