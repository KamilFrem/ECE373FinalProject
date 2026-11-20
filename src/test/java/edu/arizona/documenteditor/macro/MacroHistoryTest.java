package edu.arizona.documenteditor.macro;

import edu.arizona.documenteditor.command.CompositeCommand;
import edu.arizona.documenteditor.command.InsertTextCommand;
import edu.arizona.documenteditor.engine.DocumentEngine;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MacroHistoryTest {

    @Test
    void groupedEditsCanBeUndoneAndRedoneTogether() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("Hello");

        section.addChild(paragraph);
        document.addSection(section);

        DocumentEngine engine = new DocumentEngine(document);

        InsertTextCommand first =
                new InsertTextCommand(paragraph, 5, " world");

        InsertTextCommand second =
                new InsertTextCommand(paragraph, 11, "!");

        CompositeCommand group = new CompositeCommand(first, second);

        // Execute both edits as one history entry.
        engine.execute(group);

        assertEquals("Hello world!", paragraph.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());

        // One undo reverses both edits.
        engine.undo();

        assertEquals("Hello", paragraph.getText());
        assertEquals(0, engine.getHistoryManager().undoCount());
        assertEquals(1, engine.getHistoryManager().redoCount());

        // One redo reapplies both edits.
        engine.redo();

        assertEquals("Hello world!", paragraph.getText());
        assertEquals(1, engine.getHistoryManager().undoCount());
        assertEquals(0, engine.getHistoryManager().redoCount());
    }
}