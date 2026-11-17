package edu.arizona.documenteditor.presentation;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import edu.arizona.documenteditor.model.TextRun;
import edu.arizona.documenteditor.model.TextStyle;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkedUpRendererTest {

    private final String opening =
            "[font=Arial][size=12][color=black]";

    private final String closing =
            "[/color][/size][/font]";

    @Test
    void displaysAllFormattingOnASectionTitle() {
        TextStyle style = new TextStyle(
                true, true, "Courier", 18, "red");

        List<TextRun> title = new ArrayList<>();
        title.add(new TextRun("Title", style));

        Section section = new Section(title);
        Document document = new Document();
        document.addSection(section);

        Renderer renderer = new MarkedUpRenderer();
        String result = renderer.render(
                document, new GreedyLineBreaker(), 20);

        String expected =
                "[font=Courier][size=18][color=red]"
                        + "[bold][italic]Title[/italic][/bold]"
                        + "[/color][/size][/font]";

        assertEquals(expected, result);
    }

    @Test
    void preservesAWordSplitAcrossDifferentStyles() {
        TextStyle regular = new TextStyle(
                false, false, "Arial", 12, "black");

        TextStyle bold = new TextStyle(
                true, false, "Arial", 12, "black");

        List<TextRun> runs = new ArrayList<>();
        runs.add(new TextRun("Hel", regular));
        runs.add(new TextRun("lo world", bold));

        Paragraph paragraph = new Paragraph(runs);
        Document document = createDocument(paragraph);

        Renderer renderer = new MarkedUpRenderer();
        String result = renderer.render(
                document, new GreedyLineBreaker(), 5);

        String expected =
                opening + "Intro" + closing + "\n"
                        + opening + "Hel" + closing
                        + opening + "[bold]lo[/bold]" + closing + "\n"
                        + opening + "[bold]world[/bold]" + closing;

        assertEquals(expected, result);
    }

    @Test
    void wrapsUsingGreedyLineBreaking() {
        TextStyle style = new TextStyle(
                false, false, "Arial", 12, "black");

        Paragraph paragraph = new Paragraph("aaa bb cc ddddd", style);
        Document document = createDocument(paragraph);

        Renderer renderer = new MarkedUpRenderer();
        String result = renderer.render(
                document, new GreedyLineBreaker(), 10);

        String expected =
                opening + "Intro" + closing + "\n"
                        + opening + "aaa bb cc" + closing + "\n"
                        + opening + "ddddd" + closing;

        assertEquals(expected, result);
    }

    @Test
    void wrapsUsingBalancedLineBreaking() {
        TextStyle style = new TextStyle(
                false, false, "Arial", 12, "black");

        Paragraph paragraph = new Paragraph("aaa bb cc ddddd", style);
        Document document = createDocument(paragraph);

        Renderer renderer = new MarkedUpRenderer();
        String result = renderer.render(
                document, new BalancedLineBreaker(), 10);

        String expected =
                opening + "Intro" + closing + "\n"
                        + opening + "aaa bb" + closing + "\n"
                        + opening + "cc ddddd" + closing;

        assertEquals(expected, result);
    }

    @Test
    void rendersAnEmptyDocument() {
        Document document = new Document();
        Renderer renderer = new MarkedUpRenderer();

        String result = renderer.render(
                document, new GreedyLineBreaker(), 20);

        assertEquals("", result);
    }

    private Document createDocument(Paragraph paragraph) {
        TextStyle style = new TextStyle(
                false, false, "Arial", 12, "black");

        List<TextRun> title = new ArrayList<>();
        title.add(new TextRun("Intro", style));

        Section section = new Section(title);
        section.addChild(paragraph);

        Document document = new Document();
        document.addSection(section);

        return document;
    }
}