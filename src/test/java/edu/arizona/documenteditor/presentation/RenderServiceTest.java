package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RenderServiceTest {

    @Test
    void usesTheSelectedWidthAndLineBreaker() {
        Document document = new Document();
        Section intro = new Section("Intro");
        intro.addChild(new Paragraph("aaa bb cc ddddd"));
        document.addSection(intro);

        RenderService service = new RenderService();

        // The paragraph fits on one line at the default width of 80.
        assertEquals("Intro\naaa bb cc ddddd", service.render(document));

        service.setWidth(10);
        assertEquals("Intro\naaa bb cc\nddddd", service.render(document));

        service.setLineBreaker(new BalancedLineBreaker());
        assertEquals("Intro\naaa bb\ncc ddddd", service.render(document));
    }

    @Test
    void rejectsAWidthOfZero() {
        RenderService service = new RenderService();

        assertThrows(IllegalArgumentException.class, () -> service.setWidth(0));
    }

    @Test
    void switchesRenderersWithoutChangingTheDocument() {
        TextStyle regular =
                new TextStyle(false, false, "Arial", 12, "black");

        TextStyle bold =
                new TextStyle(true, false, "Arial", 12, "black");

        Document document = new Document();
        Section section = new Section("Intro");
        section.setRuns(java.util.List.of(new
                edu.arizona.documenteditor.model.TextRun("Intro", regular)));

        Paragraph paragraph = new Paragraph("Hello", bold);

        section.addChild(paragraph);
        document.addSection(section);

        String originalText = document.getText();

        RenderService service = new RenderService();

        service.setRenderer(new PlainTextRenderer());

        assertEquals("Intro\nHello", service.render(document));

        service.setRenderer(new MarkedUpRenderer());

        String expected =
                "[font=Arial][size=12][color=black]"
                        + "Intro[/color][/size][/font]\n"
                        + "[font=Arial][size=12][color=black][bold]"
                        + "Hello[/bold][/color][/size][/font]";

        assertEquals(expected, service.render(document));

        service.setRenderer(new PlainTextRenderer());

        assertEquals("Intro\nHello", service.render(document));

        // Displaying the document must preserve its text and formatting.
        assertEquals(originalText, document.getText());
        assertEquals(regular, section.getRuns().get(0).getStyle());
        assertEquals(bold, paragraph.getRuns().get(0).getStyle());
    }
}