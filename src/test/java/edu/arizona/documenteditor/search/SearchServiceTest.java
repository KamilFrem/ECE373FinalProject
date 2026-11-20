package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {

    private final SearchService search = new SearchService();

    @Test
    void findsSubstringsInsideLargerWords() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph("cat catalog cat");

        section.addChild(paragraph);
        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "cat", MatchMode.SUBSTRING);

        assertEquals(3, matches.size());

        assertSame(paragraph, matches.get(0).getParagraph());
        assertEquals(0, matches.get(0).getStart());
        assertEquals(3, matches.get(0).getEnd());

        assertEquals(4, matches.get(1).getStart());
        assertEquals(7, matches.get(1).getEnd());

        assertEquals(12, matches.get(2).getStart());
        assertEquals(15, matches.get(2).getEnd());
    }

    @Test
    void wholeWordSearchRejectsMatchesInsideLargerWords() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph("cat catalog cat"));
        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "cat", MatchMode.WHOLE_WORD);

        assertEquals(2, matches.size());
        assertEquals(0, matches.get(0).getStart());
        assertEquals(12, matches.get(1).getStart());
    }

    @Test
    void wholeWordSearchHandlesPunctuationAndWordCharacters() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph(
                "cat, cat! scatter cat2 cat_name"));

        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "cat", MatchMode.WHOLE_WORD);

        assertEquals(2, matches.size());
        assertEquals(0, matches.get(0).getStart());
        assertEquals(5, matches.get(1).getStart());
    }

    @Test
    void findsMatchesInNestedSectionsInDocumentOrder() {
        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph first = new Paragraph("first cat");

        Section nested = new Section("Details");
        Paragraph second = new Paragraph("second cat");
        Paragraph third = new Paragraph("third cat");

        section.addChild(first);
        nested.addChild(second);
        section.addChild(nested);
        section.addChild(third);
        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "cat", MatchMode.WHOLE_WORD);

        assertEquals(3, matches.size());
        assertSame(first, matches.get(0).getParagraph());
        assertSame(second, matches.get(1).getParagraph());
        assertSame(third, matches.get(2).getParagraph());
    }

    @Test
    void returnsNoMatchesWhenTextIsAbsent() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph("Hello world"));
        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "missing", MatchMode.SUBSTRING);

        assertTrue(matches.isEmpty());
    }

    @Test
    void searchesAnEmptyDocument() {
        Document document = new Document();

        List<SearchMatch> matches =
                search.find(document, "cat", MatchMode.SUBSTRING);

        assertTrue(matches.isEmpty());
    }

    @Test
    void searchIsCaseSensitive() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph("Cat cat CAT"));
        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "cat", MatchMode.WHOLE_WORD);

        assertEquals(1, matches.size());
        assertEquals(4, matches.get(0).getStart());
    }

    @Test
    void returnsNonOverlappingMatches() {
        Document document = new Document();
        Section section = new Section("Intro");

        section.addChild(new Paragraph("aaaaa"));
        document.addSection(section);

        List<SearchMatch> matches =
                search.find(document, "aa", MatchMode.SUBSTRING);

        assertEquals(2, matches.size());
        assertEquals(0, matches.get(0).getStart());
        assertEquals(2, matches.get(1).getStart());
    }

    @Test
    void rejectsNullArgumentsAndEmptyQuery() {
        Document document = new Document();

        assertThrows(
                NullPointerException.class,
                () -> search.find(
                        null, "cat", MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> search.find(
                        document, null, MatchMode.SUBSTRING));

        assertThrows(
                NullPointerException.class,
                () -> search.find(document, "cat", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> search.find(
                        document, "", MatchMode.SUBSTRING));
    }

    @Test
    void searchesLargeTextWithoutChangingTheDocument() {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < 10000; i++) {
            text.append("alpha beta ");
        }

        Document document = new Document();
        Section section = new Section("Intro");
        Paragraph paragraph = new Paragraph(text.toString());

        section.addChild(paragraph);
        document.addSection(section);

        String originalText = document.getText();

        assertTrue(paragraph.length() >= 100000);

        List<SearchMatch> matches =
                search.find(document, "beta", MatchMode.WHOLE_WORD);

        assertEquals(10000, matches.size());

        for (int i = 0; i < matches.size(); i++) {
            SearchMatch match = matches.get(i);

            assertSame(paragraph, match.getParagraph());
            assertEquals(i * 11 + 6, match.getStart());
            assertEquals(i * 11 + 10, match.getEnd());
        }

        assertEquals(originalText, document.getText());
    }
}