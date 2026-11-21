package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphStructureEdgeCaseTest {

    private final TextStyle regular =
            new TextStyle(false, false, "Arial", 12, "black");

    private final TextStyle bold =
            new TextStyle(true, false, "Arial", 12, "black");

    private final TextStyle italic =
            new TextStyle(false, true, "Arial", 12, "blue");

    @Test
    void splittingAtBeginningCreatesEmptyFirstParagraph() {
        Section section = new Section("Introduction");
        Paragraph paragraph = new Paragraph("Hello world");
        section.addChild(paragraph);

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, 0);

        command.execute();

        assertEquals("", paragraph.getText());
        assertEquals("Hello world",
                command.getSecondParagraph().getText());
        assertEquals(2, section.getChildren().size());
        assertSame(section, command.getSecondParagraph().getParent());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(1, section.getChildren().size());
        assertNull(command.getSecondParagraph().getParent());
    }

    @Test
    void splittingAtEndCreatesEmptySecondParagraph() {
        Section section = new Section("Introduction");
        Paragraph paragraph = new Paragraph("Hello world");
        section.addChild(paragraph);

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, paragraph.length());

        command.execute();

        assertEquals("Hello world", paragraph.getText());
        assertEquals("", command.getSecondParagraph().getText());
        assertEquals(2, section.getChildren().size());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(1, section.getChildren().size());
    }

    @Test
    void splittingStyledTextPreservesFormattingAcrossBothParagraphs() {
        Section section = new Section("Introduction");

        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("beautiful ", bold),
                new TextRun("world", italic)
        ));

        section.addChild(paragraph);

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, 15);

        command.execute();

        Paragraph second = command.getSecondParagraph();

        assertEquals("Hello beautiful", paragraph.getText());
        assertEquals(" world", second.getText());

        assertEquals(2, paragraph.getRuns().size());
        assertEquals(2, second.getRuns().size());

        // Verify formatting in the first paragraph.
        assertEquals(regular, paragraph.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());

        // Verify formatting in the second paragraph.
        assertEquals(bold, second.getRuns().get(0).getStyle());
        assertEquals(italic, second.getRuns().get(1).getStyle());

        command.undo();

        // Verify the original paragraph is restored.
        assertEquals("Hello beautiful world", paragraph.getText());
        assertEquals(3, paragraph.getRuns().size());

        assertEquals(regular, paragraph.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());
        assertEquals(italic, paragraph.getRuns().get(2).getStyle());
    }

    @Test
    void splitCanBeRedoneAfterUndo() {
        Section section = new Section("Introduction");
        Paragraph paragraph = new Paragraph("Hello world");
        section.addChild(paragraph);

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, 6);

        command.execute();
        command.undo();
        command.execute();

        assertEquals("Hello ", paragraph.getText());
        assertEquals("world",
                command.getSecondParagraph().getText());
        assertEquals(2, section.getChildren().size());
    }

    @Test
    void mergingStyledParagraphsPreservesTextAndFormatting() {
        Section section = new Section("Introduction");

        Paragraph first = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("beautiful", bold)
        ));

        Paragraph second = new Paragraph(List.of(
                new TextRun(" world", italic),
                new TextRun("!", regular)
        ));

        section.addChild(first);
        section.addChild(second);

        MergeParagraphsCommand command =
                new MergeParagraphsCommand(section, first, second);

        command.execute();

        assertEquals(1, section.getChildren().size());
        assertEquals("Hello beautiful world!", first.getText());
        assertEquals(4, first.getRuns().size());

        assertEquals(regular, first.getRuns().get(0).getStyle());
        assertEquals(bold, first.getRuns().get(1).getStyle());
        assertEquals(italic, first.getRuns().get(2).getStyle());
        assertEquals(regular, first.getRuns().get(3).getStyle());

        assertNull(second.getParent());

        command.undo();

        assertEquals(2, section.getChildren().size());
        assertEquals("Hello beautiful", first.getText());
        assertEquals(" world!", second.getText());
        assertSame(section, second.getParent());

        assertEquals(2, first.getRuns().size());
        assertEquals(2, second.getRuns().size());
        assertEquals(italic, second.getRuns().get(0).getStyle());
        assertEquals(regular, second.getRuns().get(1).getStyle());
    }

    @Test
    void mergeCanBeRedoneAfterUndo() {
        Section section = new Section("Introduction");
        Paragraph first = new Paragraph("Hello");
        Paragraph second = new Paragraph(" world");

        section.addChild(first);
        section.addChild(second);

        MergeParagraphsCommand command =
                new MergeParagraphsCommand(section, first, second);

        command.execute();
        command.undo();
        command.execute();

        assertEquals(1, section.getChildren().size());
        assertEquals("Hello world", first.getText());
        assertNull(second.getParent());
    }

    @Test
    void rejectsSplittingParagraphThatDoesNotBelongToSection() {
        Paragraph paragraph = new Paragraph("Hello world");

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, 5);

        assertThrows(
                IllegalArgumentException.class,
                command::execute
        );

        assertEquals("Hello world", paragraph.getText());
        assertNull(paragraph.getParent());
    }

    @Test
    void rejectsSplitOutsideParagraphBoundsWithoutChangingStructure() {
        Section section = new Section("Introduction");
        Paragraph paragraph = new Paragraph("Hello world");
        section.addChild(paragraph);

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, 20);

        assertThrows(
                IndexOutOfBoundsException.class,
                command::execute
        );

        assertEquals("Hello world", paragraph.getText());
        assertEquals(1, section.getChildren().size());
    }
}
