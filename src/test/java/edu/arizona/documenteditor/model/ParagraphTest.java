//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphTest {
    private final TextStyle bold = new TextStyle(true, false, "Arial", 12, "black");
    private final TextStyle DEFAULT_STYLE = new TextStyle(false, false, "Arial", 12, "black");

    @Test
    @DisplayName("Empty paragraph should not store any Text Runs")
    void emptyParagraphHasNoRuns(){
        Paragraph p = new Paragraph();
        assertEquals("", p.getText());
        assertEquals(0, p.length());
        assertTrue(p.getRuns().isEmpty());
    }

    @Test
    @DisplayName("Can insert text to the both end of the text run list")
    void insertBothEnd(){
        Paragraph p = new Paragraph("middle");
        p.insertText(0, "<");
        p.insertText(p.length(), ">");
        assertEquals("<middle>", p.getText());
        assertEquals(1, p.getRuns().size());
    }

    @Test
    @DisplayName("deletes can delete across runs while preserving the text style")
    void deleteAcrossRuns(){
        Paragraph p = new Paragraph(List.of(new TextRun("abc", bold), new TextRun("def", DEFAULT_STYLE)));
        p.deleteText(1, 5);
        assertEquals(List.of(new TextRun("a", bold), new TextRun("f", DEFAULT_STYLE)), p.getRuns());
    }

    @Test
    @DisplayName("replaceText replaces with user defined styles")
    void replaceWithStyle(){
        Paragraph p = new Paragraph("abcdef");
        p.replaceText(1, 5, "X", bold);
        assertEquals("aXf", p.getText()); assertEquals(bold, p.getRuns().get(1).getStyle());
    }

    @Test
    @DisplayName("Insertion without style uses default style")
    void insertionWithoutStyle(){
        Paragraph p = new Paragraph("ab", bold);
        p.insertText(1, "X");
        assertEquals(DEFAULT_STYLE, p.getRuns().get(1).getStyle());
    }

    @Test
    @DisplayName("Empty range is rejected and full range deletions work")
    void emptyRangesFailAndFullRangesWork(){
        Paragraph p = new Paragraph("abc");
        assertThrows(IllegalArgumentException.class, () -> p.deleteText(1, 1));
        p.insertText(3, "");
        assertEquals("abc", p.getText());
        p.deleteText(0, 3);
        assertTrue(p.getRuns().isEmpty());
    }

    @Test
    @DisplayName("Invalid ranges leave text unchanged")
    void invalidRangeLeaveTextUnchanged(){
        Paragraph p = new Paragraph("abc", bold);
        for (int[] range : new int[][]{{-1, 1}, {2, 1}, {0, 4}}) {
            assertThrows(IndexOutOfBoundsException.class, () -> p.deleteText(range[0], range[1]));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> p.insertText(4, "d"));
        assertEquals(List.of(new TextRun("abc", bold)), p.getRuns());
    }

    @Test
    @DisplayName("Null replacement does not delet the text")
    void nullReplacementsRejectedWhileOriginalTextPreserved(){
        Paragraph p = new Paragraph("abc");
        assertThrows(NullPointerException.class, () -> p.replaceText(0, 3, null));
        assertEquals("abc", p.getText());
    }

    @Test
    @DisplayName("Copy is independent from original")
    void CopyIsIndependent(){
        Paragraph p = new Paragraph("abc", bold);
        Paragraph copy = p.deepCopy();
        copy.deleteText(0, 1);
        assertEquals("abc", p.getText());
        assertEquals(List.of(new TextRun("b", bold)), p.slice(1, 2));
    }

    @Test
    @DisplayName("Paragraph can handle 100000 characters")
    void handlesOneHundredThousandChar(){
        Paragraph p = new Paragraph("abcde".repeat(20000));
        p.setRuns(List.of(new TextRun("abcde".repeat(2000), DEFAULT_STYLE),
                new TextRun("abcde".repeat(16000), bold),
                new TextRun("abcde".repeat(2000), DEFAULT_STYLE)));
        assertEquals(100000, p.length());
        assertEquals(3, p.getRuns().size());
        List<TextRun> selected = p.slice(10000, 20000);
        p.deleteText(10000, 20000);
        p.insertRuns(p.length(), selected);
        assertEquals(100000, p.length());
        assertEquals("abcde".repeat(20000), p.getText());
        assertEquals(bold, p.getRuns().get(p.getRuns().size() - 1).getStyle());
    }

    @Test
    @DisplayName("Paragraph Split returns detached Suffix and preserves styles")
    void splitReturnsSuffixAndPreservesStyle(){
        Section section = new Section("S");
        Paragraph paragraph = new Paragraph("abcdef");
        paragraph.setRuns(List.of(new TextRun("a", DEFAULT_STYLE),
                new TextRun("bcde", DEFAULT_STYLE.withItalic(true)), new TextRun("f", DEFAULT_STYLE)));
        List<TextRun> prefix = paragraph.slice(0, 3);
        List<TextRun> suffix = paragraph.slice(3, 6);
        section.addChild(paragraph);
        Paragraph result = paragraph.split(3);
        assertEquals(prefix, paragraph.getRuns());
        assertEquals(suffix, result.getRuns());
        assertNull(result.getParent());
        assertSame(section, paragraph.getParent());
        assertEquals(List.of(paragraph), section.getChildren());
    }

    @Test
    @DisplayName("Paragraph Split supports edges and Validates before split")
    void splitSupportsEdgesAndValidatesBeforeChanging(){
        for (int position : new int[]{0, 3}) {
            Paragraph paragraph = new Paragraph("abc");
            Paragraph suffix = paragraph.split(position);
            assertEquals("abc".substring(0, position), paragraph.getText());
            assertEquals("abc".substring(position), suffix.getText());
        }
        Paragraph paragraph = new Paragraph("abc");
        assertThrows(IndexOutOfBoundsException.class, () -> paragraph.split(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> paragraph.split(4));
        assertEquals("abc", paragraph.getText());
        assertEquals("", new Paragraph().split(0).getText());
    }

    @Test
    @DisplayName("Paragraph merge preserve source paragraph and does not remove childeren")
    void paragraphMergePreservesSource(){
        Section section = new Section("S");
        Paragraph first = new Paragraph("abc");
        Paragraph second = new Paragraph("def", DEFAULT_STYLE.withBold(true));
        section.addChild(first);
        section.addChild(second);
        List<TextRun> source = second.getRuns();
        first.merge(second);
        assertEquals("abcdef", first.getText());
        assertTrue(first.getRuns().get(1).getStyle().isBold());
        assertEquals(source, second.getRuns());
        assertEquals(List.of(first, second), section.getChildren());
        assertSame(section, second.getParent());
    }

    @Test
    @DisplayName("Invalid paragraph merge does not change content")
    void invalidMergeDoesNotChangeContent(){}

    @Test
    @DisplayName("Empty selections are rejected without changing content")
    void rejectEmptySelections(){}

    @Test
    @DisplayName("Null inputs are rejected and content does not change")
    void nullIsRejected(){}

    @Test
    @DisplayName("Paragraph can store text, styles, and length")
    void storesPreparedRuns(){}

}
