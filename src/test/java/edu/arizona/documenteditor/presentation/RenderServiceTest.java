package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
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
}