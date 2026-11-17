package edu.arizona.documenteditor.command;

import edu.arizona.documenteditor.history.HistoryManager;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private final TextStyle regular =
            new TextStyle(false, false, "Arial", 12, "black");

    private final TextStyle bold =
            new TextStyle(true, false, "Arial", 12, "black");

    @Test
    void createsAndUndoesSection() {
        Document document = new Document();
        CreateSectionCommand command =
                new CreateSectionCommand(document, 0, "Introduction");

        command.execute();

        assertEquals(1, document.getSections().size());
        assertEquals("Introduction", document.getSections().get(0).getText());

        command.undo();

        assertTrue(document.getSections().isEmpty());
    }

    @Test
    void createsAndUndoesParagraph() {
        Section section = new Section("Introduction");

        CreateParagraphCommand command =
                new CreateParagraphCommand(section, 0, "Hello world");

        command.execute();

        assertEquals(1, section.getChildren().size());
        assertEquals("Hello world", section.getChildren().get(0).getText());

        command.undo();

        assertTrue(section.getChildren().isEmpty());
    }

    @Test
    void insertsAndUndoesText() {
        Paragraph paragraph = new Paragraph("Hello");

        InsertTextCommand command =
                new InsertTextCommand(paragraph, 5, " world", bold);

        command.execute();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());

        command.undo();

        assertEquals("Hello", paragraph.getText());
    }

    @Test
    void deletesAndRestoresStyledText() {
        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("world", bold)
        ));

        DeleteTextCommand command =
                new DeleteTextCommand(paragraph, 3, 8);

        command.execute();

        assertEquals("Helrld", paragraph.getText());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(2, paragraph.getRuns().size());
        assertEquals(regular, paragraph.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());
    }

    @Test
    void replacesAndRestoresText() {
        Paragraph paragraph = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("world", bold)
        ));

        ReplaceTextCommand command =
                new ReplaceTextCommand(paragraph, 6, 11, "Java", regular);

        command.execute();

        assertEquals("Hello Java", paragraph.getText());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(bold, paragraph.getRuns().get(1).getStyle());
    }

    @Test
    void movesTextBetweenParagraphsAndPreservesStyle() {
        Paragraph source = new Paragraph(List.of(
                new TextRun("Hello ", regular),
                new TextRun("world", bold)
        ));

        Paragraph destination = new Paragraph("Today");

        MoveTextCommand command =
                new MoveTextCommand(source, 6, 11, destination, 5);

        command.execute();

        assertEquals("Hello ", source.getText());
        assertEquals("Todayworld", destination.getText());
        assertEquals(bold, destination.getRuns().get(1).getStyle());

        command.undo();

        assertEquals("Hello world", source.getText());
        assertEquals("Today", destination.getText());
        assertEquals(bold, source.getRuns().get(1).getStyle());
    }

    @Test
    void movesTextWithinSameParagraph() {
        Paragraph paragraph = new Paragraph("one two three");

        MoveTextCommand command =
                new MoveTextCommand(paragraph, 0, 3, paragraph, 13);

        command.execute();

        assertEquals(" two threeone", paragraph.getText());

        command.undo();

        assertEquals("one two three", paragraph.getText());
    }

    @Test
    void rejectsMovingTextIntoItsOwnRange() {
        Paragraph paragraph = new Paragraph("one two three");

        MoveTextCommand command =
                new MoveTextCommand(paragraph, 0, 3, paragraph, 2);

        assertThrows(
                IllegalArgumentException.class,
                command::execute
        );

        assertEquals("one two three", paragraph.getText());
    }

    @Test
    void splitsAndUndoesParagraph() {
        Section section = new Section("Introduction");
        Paragraph paragraph = new Paragraph("Hello world");
        section.addChild(paragraph);

        SplitParagraphCommand command =
                new SplitParagraphCommand(paragraph, 6);

        command.execute();

        assertEquals("Hello ", paragraph.getText());
        assertEquals("world", command.getSecondParagraph().getText());
        assertEquals(2, section.getChildren().size());

        command.undo();

        assertEquals("Hello world", paragraph.getText());
        assertEquals(1, section.getChildren().size());
    }

    @Test
    void mergesAndUndoesParagraphs() {
        Section section = new Section("Introduction");

        Paragraph first = new Paragraph(List.of(
                new TextRun("Hello ", regular)
        ));

        Paragraph second = new Paragraph(List.of(
                new TextRun("world", bold)
        ));

        section.addChild(first);
        section.addChild(second);

        MergeParagraphsCommand command =
                new MergeParagraphsCommand(section, first, second);

        command.execute();

        assertEquals(1, section.getChildren().size());
        assertEquals("Hello world", first.getText());

        command.undo();

        assertEquals(2, section.getChildren().size());
        assertEquals("Hello ", first.getText());
        assertEquals("world", second.getText());
        assertEquals(bold, second.getRuns().get(0).getStyle());
    }

    @Test
    void rejectsMergingNonAdjacentParagraphs() {
        Section section = new Section("Introduction");

        Paragraph first = new Paragraph("First");
        Paragraph middle = new Paragraph("Middle");
        Paragraph last = new Paragraph("Last");

        section.addChild(first);
        section.addChild(middle);
        section.addChild(last);

        MergeParagraphsCommand command =
                new MergeParagraphsCommand(section, first, last);

        assertThrows(
                IllegalArgumentException.class,
                command::execute
        );

        assertEquals(3, section.getChildren().size());
    }

    @Test
    void compositeCommandUndoesMultipleOperationsTogether() {
        Document document = new Document();

        // Create the section first so we can use the same instance
        // when creating the paragraph.
        CreateSectionCommand createSection =
                new CreateSectionCommand(document, 0, "Introduction");

        CompositeCommand actual = new CompositeCommand(
                createSection,
                new CreateParagraphCommand(
                        createSection.getSection(), 0, "Hello")
        );

        actual.execute();

        assertEquals(1, document.getSections().size());
        assertEquals(
                "Hello",
                document.getSections().get(0).getChildren().get(0).getText()
        );

        actual.undo();

        assertTrue(document.getSections().isEmpty());
    }

    @Test
    void failedCompositeCommandRollsBackCompletedCommands() {
        Document document = new Document();

        CreateSectionCommand createSection =
                new CreateSectionCommand(document, 0, "Introduction");

        CreateParagraphCommand invalidParagraph =
                new CreateParagraphCommand(
                        createSection.getSection(), 99, "Hello");

        CompositeCommand composite =
                new CompositeCommand(createSection, invalidParagraph);

        HistoryManager history = new HistoryManager();

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> history.executeCommand(composite)
        );

        assertTrue(document.getSections().isEmpty());
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }
}