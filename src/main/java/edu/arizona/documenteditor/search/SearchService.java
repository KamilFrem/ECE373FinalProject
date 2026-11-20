package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.DocumentNode;
import edu.arizona.documenteditor.model.FormatTarget;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;

import java.util.ArrayList;
import java.util.List;

/** Finds case-sensitive matches in section titles and paragraphs. */
public final class SearchService {

    /**
     * Finds non-overlapping matches in document order.
     * Each section title is searched before its children.
     *
     * @param document the document to search
     * @param query the text to find
     * @param mode substring or whole-word matching
     * @return matching ranges, or an empty list
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
        List<Section> sections = document.getSections();

        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            findSection(section, query, mode, matches);
        }

        return matches;
    }

    private void findSection(
            Section section,
            String query,
            MatchMode mode,
            List<SearchMatch> matches) {

        // Search this section's title.
        findTarget(section, query, mode, matches);

        List<DocumentNode> children = section.getChildren();

        for (int i = 0; i < children.size(); i++) {
            DocumentNode child = children.get(i);

            if (child.getClass() == Section.class) {
                Section nestedSection = (Section) child;
                findSection(nestedSection, query, mode, matches);
            }
            else if (child.getClass() == Paragraph.class) {
                Paragraph paragraph = (Paragraph) child;
                findTarget(paragraph, query, mode, matches);
            }
        }
    }

    private void findTarget(
            FormatTarget target,
            String query,
            MatchMode mode,
            List<SearchMatch> matches) {

        String text = target.getText();
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

                SearchMatch match = new SearchMatch(target, start, end);

                matches.add(match);
            }

            start = text.indexOf(query, end);
        }
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