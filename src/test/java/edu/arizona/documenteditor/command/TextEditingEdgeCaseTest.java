package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextEditingEdgeCaseTest {

    private final TextStyle regular =
            new TextStyle(false, false, "Arial", 12, "black");

    private final TextStyle bold =
            new TextStyle(true, false, "Arial", 12, "black");

    private final TextStyle italic =
            new TextStyle(false, true, "Arial", 12, "blue");

    @Test
    void insertsTextAtBeginningAndCanUndoAndRedo() {
        Paragraph paragraph = new Paragraph("world");

        InsertTextCommand command =
                new InsertTextCommand(paragraph, 0, "Hello ", bold);

        command.execute();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(bold, paragraph.getRuns().get(0).getStyle());

        command.undo();

        assertEquals("world", paragraph.getText());

        command.execute();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(bold, paragraph.getRuns().get(0).getStyle());
    }

    @Test
    void insertsTextAtEndWithoutChangingExistingFormatting() {
        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Hello", italic)
        ));

        InsertTextCommand command =
                new InsertTextCommand(paragraph, 5, " world", bold);

        command.execute();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(2, paragraph.getRuns().size());
        assertEquals(italic, paragraph.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());

        command.undo();

        assertEquals("Hello", paragraph.getText());
        assertEquals(italic, paragraph.getRuns().get(0).getStyle());
    }

    @Test
    void rejectsInsertionOutsideParagraphBounds() {
        Paragraph paragraph = new Paragraph("Hello");

        InsertTextCommand command =
                new InsertTextCommand(paragraph, 10, " world", regular);

        assertThrows(
                IndexOutOfBoundsException.class,
                command::execute
        );

        assertEquals("Hello", paragraph.getText());
    }

    @Test
    void deletesTextAcrossMultipleStylesAndRestoresOriginalRuns() {
        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("beautiful ", bold),
                new TextRun("world", italic)
        ));

        DeleteTextCommand command =
                new DeleteTextCommand(paragraph, 3, 16);

        command.execute();

        assertEquals("Helworld", paragraph.getText());
        assertEquals(2, paragraph.getRuns().size());

        command.undo();

        assertEquals("Hello beautiful world", paragraph.getText());
        assertEquals(3, paragraph.getRuns().size());
        assertEquals(regular, paragraph.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());
        assertEquals(italic, paragraph.getRuns().get(2).getStyle());

        command.execute();

        assertEquals("Helworld", paragraph.getText());
    }

    @Test
    void rejectsInvalidDeletionWithoutChangingParagraph() {
        Paragraph paragraph = new Paragraph("Hello world");

        DeleteTextCommand command =
                new DeleteTextCommand(paragraph, 3, 20);

        assertThrows(
                IndexOutOfBoundsException.class,
                command::execute
        );

        assertEquals("Hello world", paragraph.getText());
    }

    @Test
    void replacesTextWithDifferentLengthAndRestoresOriginalFormatting() {
        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("world", bold)
        ));

        ReplaceTextCommand command =
                new ReplaceTextCommand(
                        paragraph, 6, 11, "universe", italic);

        command.execute();

        assertEquals("Hello universe", paragraph.getText());
        assertEquals(2, paragraph.getRuns().size());
        assertEquals(regular, paragraph.getRuns().get(0).getStyle());
        assertEquals(italic, paragraph.getRuns().get(1).getStyle());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(2, paragraph.getRuns().size());
        assertEquals(regular, paragraph.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());

        command.execute();

        assertEquals("Hello universe", paragraph.getText());
    }

    @Test
    void rejectsInvalidReplacementWithoutChangingParagraph() {
        Paragraph paragraph = new Paragraph("Hello world");

        ReplaceTextCommand command =
                new ReplaceTextCommand(
                        paragraph, 6, 20, "universe", regular);

        assertThrows(
                IndexOutOfBoundsException.class,
                command::execute
        );

        assertEquals("Hello world", paragraph.getText());
    }

    @Test
    void replacingTextWithEmptyStringDeletesSelectedText() {
        Paragraph paragraph = new Paragraph("Hello world");

        ReplaceTextCommand command =
                new ReplaceTextCommand(
                        paragraph, 5, 11, "");

        command.execute();

        assertEquals("Hello", paragraph.getText());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
    }
}