package edu.arizona.documenteditor.statistics;

import edu.arizona.documenteditor.engine.DocumentChangeListener;
import edu.arizona.documenteditor.model.Document;
import edu.arizona.documenteditor.model.DocumentNode;
import edu.arizona.documenteditor.model.Paragraph;
import edu.arizona.documenteditor.model.Section;

import java.util.List;

/** Counts words, characters, and paragraphs in a document. */
public final class DocumentStatistics implements DocumentChangeListener {
    private int wordCount;
    private int characterCount;
    private int paragraphCount;

    /**
     * Calculates the initial document statistics.
     *
     * @param document the document to count
     */
    public DocumentStatistics(Document document) {
        onDocumentChanged(document);
    }

    /** {@inheritDoc} */
    @Override
    public void onDocumentChanged(Document document) {
        if (document == null) {
            throw new NullPointerException("Document cannot be null.");
        }

        wordCount = 0;
        characterCount = 0;
        paragraphCount = 0;

        List<Section> sections = document.getSections();

        for (int i = 0; i < sections.size(); i++) {
            countSection(sections.get(i));
        }
    }

    private void countSection(Section section) {
        countText(section.getText());

        List<DocumentNode> children = section.getChildren();

        for (int i = 0; i < children.size(); i++) {
            DocumentNode child = children.get(i);

            if (child.getClass() == Section.class) {
                Section nestedSection = (Section) child;
                countSection(nestedSection);
            }

            if (child.getClass() == Paragraph.class) {
                Paragraph paragraph = (Paragraph) child;
                paragraphCount++;
                countText(paragraph.getText());
            }
        }
    }

    private void countText(String text) {
        characterCount = characterCount + text.length();

        boolean insideWord = false;

        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);

            if (Character.isWhitespace(character)) {
                insideWord = false;
            }

            else {
                if (insideWord == false) {
                    wordCount++;
                    insideWord = true;
                }
            }
        }
    }

    /**
     * Returns the current word count.
     *
     * @return the number of words
     */
    public int getWordCount() {
        return wordCount;
    }

    /**
     * Returns the current character count.
     *
     * @return the number of stored text characters
     */
    public int getCharacterCount() {
        return characterCount;
    }

    /**
     * Returns the current paragraph count.
     *
     * @return the number of paragraphs, including empty paragraphs
     */
    public int getParagraphCount() {
        return paragraphCount;
    }
}
