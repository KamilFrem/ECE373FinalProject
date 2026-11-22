package edu.arizona.documenteditor.formatting;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatCommandTest {

    @Test
    void formatsOnlyTheSelectedText() {
        TextStyle regular = new TextStyle(
                false, false, "Arial", 12, "black");

        Paragraph paragraph = new Paragraph("Hello world", regular);

        FormatCommand command =
                new FormatCommand(paragraph, 0, 5, "bold", "true");

        command.execute();

        List<TextRun> expected = new ArrayList<>();
        expected.add(new TextRun("Hello", regular.withBold(true)));
        expected.add(new TextRun(" world", regular));

        assertEquals("Hello world", paragraph.getText());
        assertEquals(expected, paragraph.getRuns());
    }

    @Test
    void formatsAcrossRunsAndPreservesOtherAttributes() {
        TextStyle firstStyle = new TextStyle(
                false, false, "Arial", 12, "black");

        TextStyle secondStyle = new TextStyle(
                false, true, "Courier", 18, "red");

        List<TextRun> runs = new ArrayList<>();
        runs.add(new TextRun("Hello", firstStyle));
        runs.add(new TextRun(" world", secondStyle));

        Paragraph paragraph = new Paragraph(runs);

        FormatCommand command =
                new FormatCommand(paragraph, 3, 8, "bold", "true");

        command.execute();

        List<TextRun> expected = new ArrayList<>();
        expected.add(new TextRun("Hel", firstStyle));
        expected.add(new TextRun("lo", firstStyle.withBold(true)));
        expected.add(new TextRun(" wo", secondStyle.withBold(true)));
        expected.add(new TextRun("rld", secondStyle));

        assertEquals("Hello world", paragraph.getText());
        assertEquals(expected, paragraph.getRuns());

        command.undo();
        assertEquals(runs, paragraph.getRuns());

        // Executing again after undo checks redo behavior.
        command.execute();
        assertEquals(expected, paragraph.getRuns());
    }

    @Test
    void formatsASectionTitle() {
        Section section = new Section("Introduction");

        List<TextRun> original = new ArrayList<>(section.getRuns());
        TextStyle originalStyle = original.get(0).getStyle();

        FormatCommand command =
                new FormatCommand(section, 0, 12, "fontSize", "20");

        command.execute();

        assertEquals("Introduction", section.getText());
        assertEquals(originalStyle.withFontSize(20),
                section.getRuns().get(0).getStyle());

        command.undo();
        assertEquals(original, section.getRuns());
    }

    @Test
    void rejectsAnInvalidRangeWithoutChangingTheTextOrStyles() {
        Paragraph paragraph = new Paragraph("Hello");
        List<TextRun> original = new ArrayList<>(paragraph.getRuns());

        FormatCommand command =
                new FormatCommand(paragraph, 0, 20, "bold", "true");

        assertThrows(IllegalArgumentException.class, () -> command.execute());

        assertEquals("Hello", paragraph.getText());
        assertEquals(original, paragraph.getRuns());
    }

    @Test
    void rejectsAnInvalidValueWithoutChangingTheTextOrStyles() {
        Paragraph paragraph = new Paragraph("Hello");
        List<TextRun> original = new ArrayList<>(paragraph.getRuns());

        FormatCommand command =
                new FormatCommand(paragraph, 0, 5, "bold", "yes");

        assertThrows(IllegalArgumentException.class, () -> command.execute());

        assertEquals(original, paragraph.getRuns());
    }

    @Test
    void changesItalicAndSupportsUndoAndRedo() {
        TextStyle originalStyle =
                new TextStyle(false, false, "Arial", 12, "black");

        Paragraph paragraph = new Paragraph("Hello", originalStyle);

        FormatCommand command =
                new FormatCommand(paragraph, 0, 5, "italic", "true");

        command.execute();

        assertEquals("Hello", paragraph.getText());
        assertEquals(
                originalStyle.withItalic(true),
                paragraph.getRuns().get(0).getStyle());

        command.undo();

        assertEquals(originalStyle, paragraph.getRuns().get(0).getStyle());

        command.execute();

        assertEquals(
                originalStyle.withItalic(true),
                paragraph.getRuns().get(0).getStyle());
    }

    @Test
    void changesFontNameAndPreservesOtherFormatting() {
        TextStyle originalStyle =
                new TextStyle(true, true, "Arial", 18, "red");

        Paragraph paragraph = new Paragraph("Hello", originalStyle);

        FormatCommand command =
                new FormatCommand(paragraph, 0, 5, "fontName", "Courier");

        command.execute();

        assertEquals("Hello", paragraph.getText());
        assertEquals(
                originalStyle.withFontName("Courier"),
                paragraph.getRuns().get(0).getStyle());

        command.undo();

        assertEquals(originalStyle, paragraph.getRuns().get(0).getStyle());
    }

    @Test
    void changesColorOnlyWithinTheSelectedRange() {
        TextStyle originalStyle =
                new TextStyle(true, false, "Arial", 12, "black");

        Paragraph paragraph = new Paragraph("Hello world", originalStyle);

        FormatCommand command =
                new FormatCommand(paragraph, 0, 5, "color", "blue");

        command.execute();

        List<TextRun> expected = new ArrayList<TextRun>();
        expected.add(new TextRun("Hello", originalStyle.withColor("blue")));
        expected.add(new TextRun(" world", originalStyle));

        assertEquals("Hello world", paragraph.getText());
        assertEquals(expected, paragraph.getRuns());

        command.undo();

        List<TextRun> original = new ArrayList<TextRun>();
        original.add(new TextRun("Hello world", originalStyle));

        assertEquals(original, paragraph.getRuns());
    }
}