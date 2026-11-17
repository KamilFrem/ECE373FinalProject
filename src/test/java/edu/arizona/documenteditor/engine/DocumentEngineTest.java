package edu.arizona.documenteditor.engine;

import edu.arizona.documenteditor.command.CreateSectionCommand;
import edu.arizona.documenteditor.model.Document;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DocumentEngineTest {

    @Test
    void executesCommandsAndNotifiesListeners() {
        Document document = new Document();
        DocumentEngine engine = new DocumentEngine(document);

        AtomicInteger notifications = new AtomicInteger();

        engine.addChangeListener(changedDocument -> {
            assertSame(document, changedDocument);
            notifications.incrementAndGet();
        });

        engine.execute(
                new CreateSectionCommand(document, 0, "Introduction"));

        assertEquals(1, notifications.get());
        assertEquals(1, document.getSections().size());
    }

    @Test
    void undoAndRedoNotifyListeners() {
        DocumentEngine engine = new DocumentEngine();

        AtomicInteger notifications = new AtomicInteger();

        engine.addChangeListener(
                document -> notifications.incrementAndGet());

        engine.execute(
                new CreateSectionCommand(
                        engine.getDocument(), 0, "Introduction"));

        engine.undo();

        assertTrue(engine.getDocument().getSections().isEmpty());
        assertEquals(2, notifications.get());

        engine.redo();

        assertEquals(1, engine.getDocument().getSections().size());
        assertEquals(3, notifications.get());
    }

    @Test
    void failedCommandDoesNotNotifyListeners() {
        DocumentEngine engine = new DocumentEngine();

        AtomicInteger notifications = new AtomicInteger();

        engine.addChangeListener(
                document -> notifications.incrementAndGet());

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> engine.execute(
                        new CreateSectionCommand(
                                engine.getDocument(), 5, "Invalid"))
        );

        assertEquals(0, notifications.get());
        assertTrue(engine.getDocument().getSections().isEmpty());
        assertFalse(engine.canUndo());
    }

    @Test
    void removesListeners() {
        DocumentEngine engine = new DocumentEngine();

        AtomicInteger notifications = new AtomicInteger();

        DocumentChangeListener listener =
                document -> notifications.incrementAndGet();

        engine.addChangeListener(listener);

        assertTrue(engine.removeChangeListener(listener));

        engine.execute(
                new CreateSectionCommand(
                        engine.getDocument(), 0, "Introduction"));

        assertEquals(0, notifications.get());
    }
}