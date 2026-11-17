package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlainTextRendererTest {

    @Test
    void rendersTheSameDocumentWithBothLineBreakers() {
        Document document = new Document();
        Section intro = new Section("Intro");
        intro.addChild(new Paragraph("aaa bb cc ddddd"));
        document.addSection(intro);

        Renderer renderer = new PlainTextRenderer();

        String greedy = renderer.render(document, new GreedyLineBreaker(), 10);
        String balanced = renderer.render(document, new BalancedLineBreaker(), 10);

        assertEquals("Intro\naaa bb cc\nddddd", greedy);
        assertEquals("Intro\naaa bb\ncc ddddd", balanced);
    }

    @Test
    void rendersNestedSectionsInDocumentOrder() {
        Document document = new Document();
        Section intro = new Section("Intro");
        Section details = new Section("Details");

        intro.addChild(new Paragraph("First paragraph"));
        details.addChild(new Paragraph("Nested paragraph"));
        intro.addChild(details);
        intro.addChild(new Paragraph("Last paragraph"));
        document.addSection(intro);

        Renderer renderer = new PlainTextRenderer();
        String result = renderer.render(document, new GreedyLineBreaker(), 30);

        assertEquals("Intro\nFirst paragraph\nDetails\nNested paragraph\nLast paragraph",
                result);
    }

    @Test
    void rendersAnEmptyDocumentAsAnEmptyString() {
        Renderer renderer = new PlainTextRenderer();
        String result = renderer.render(new Document(), new GreedyLineBreaker(), 10);

        assertEquals("", result);
    }
}