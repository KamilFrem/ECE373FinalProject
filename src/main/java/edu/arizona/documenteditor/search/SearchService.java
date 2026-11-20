package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.Paragraph;

import java.util.ArrayList;
import java.util.List;

/** Finds case-sensitive matches in document paragraphs. */
public final class SearchService {

    /**
     * Finds non-overlapping matches in document order.
     * Section titles are not searched by this method.
     *
     * @param document the document to search
     * @param query the text to find
     * @param mode substring or whole-word matching
     * @return matching paragraph ranges, or an empty list
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if query is empty
     */
    public List<SearchMatch> find(
            Document document, String query, MatchMode mode) {

        if (document == null) {
            throw new NullPointerException("Document cannot be null.");
        }

        if (query == null) {
            throw new NullPointerException("Search text cannot be null.");
        }

        if (mode == null) {
            throw new NullPointerException("Match mode cannot be null.");
        }

        if (query.isEmpty()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }

        List<SearchMatch> matches = new ArrayList<SearchMatch>();
        List<Paragraph> paragraphs = document.getParagraphs();

        for (int i = 0; i < paragraphs.size(); i++) {
            Paragraph paragraph = paragraphs.get(i);
            String text = paragraph.getText();

            int start = text.indexOf(query);

            while (start != -1) {
                int end = start + query.length();
                boolean includeMatch = false;

                if (mode == MatchMode.SUBSTRING) {
                    includeMatch = true;
                }
                else if (mode == MatchMode.WHOLE_WORD) {
                    includeMatch = isWholeWord(text, start, end);
                }

                if (includeMatch) {
                    SearchMatch match =
                            new SearchMatch(paragraph, start, end);

                    matches.add(match);
                }

                start = text.indexOf(query, end);
            }
        }

        return matches;
    }

    private boolean isWholeWord(String text, int start, int end) {
        if (start > 0) {
            char before = text.charAt(start - 1);

            if (isWordCharacter(before)) {
                return false;
            }
        }

        if (end < text.length()) {
            char after = text.charAt(end);

            if (isWordCharacter(after)) {
                return false;
            }
        }

        return true;
    }

    private boolean isWordCharacter(char character) {
        if (Character.isLetterOrDigit(character)) {
            return true;
        }

        if (character == '_') {
            return true;
        }

        return false;
    }
}