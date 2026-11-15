//------------------------------------------------------
// FinalProject
// Written by: Group 8
// ECE 373 Object-Oriented Software Design, Section 001 - Fall 2026
// The University of Arizona
//------------------------------------------------------
package edu.arizona.documenteditor.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores editable text as immutable runs.
 * All ranges are zero based and end exclusive
 * Command classes will coordinate the undo and redo operations this class only creates paragraph
 */
public final class Paragraph implements DocumentNode,FormatTarget {
    /** Stores section that owns this paragraph. */
    Section parent;
    /** Stores style and text of the paragraph. */
    private List<TextRun> runs = List.of();
    /** Cached length of the paragraph. */
    private int length;
    /** default font style is normal Arial font text with size 12. */
    private final TextStyle DEFAULT_STYLE = new TextStyle(false, false, "Arial", 12, "black");

    /**
     * Creates empty paragraph.
     */
    public Paragraph(){}

    /**
     * Creates a detached paragraph using default style.
     * @param text initial paragraph text.
     * @throws NullPointerException if input is null.
     */
    public Paragraph(final String text){
        this(text, new TextStyle(false, false, "Arial", 12, "black"));
    }

    /**
     * Creates a detached paragraph with user defined initial text and style.
     * @param text initial paragraph text.
     * @param style initial text style.
     * @throws NullPointerException if one of the inputs are null.
     */
    public Paragraph(final String text, final TextStyle style){
        setRuns(List.of(new TextRun(text, style)));
    }

    /**
     * Creates a detached paragraph from a copy of runs. This allows multiple styles in one paragraph.
     * @param initialRuns initial text and styles.
     * @throws NullPointerException if input is null.
     * @throws ArithmeticException if the combined text length exceeds the integer range.
     */
    public Paragraph(final List<TextRun> initialRuns){ setRuns(initialRuns); }

    /**
     * Returns the section that owns this paragraph.
     * @return the owning section or {@code null} when no parent exists.
     */
    @Override
    public Section getParent(){return parent;}

    /**
     * Returns the length of the stored text.
     * @return number of characters zero for empty text.
     */
    @Override
    public int length(){return length;}

    /**
     * Returns an immutable snapshot of the text runs.
     * Both the list and its values can be retained by commands to undo and redo operations.
     * @return the text runs.
     */
    @Override
    public List<TextRun> getRuns(){return runs;}

    /**
     * Returns the text stored in paragraph without preserving the style and formatting values.
     * @return text available in paragraph.
     */
    @Override
    public String getText(){
        StringBuilder result = new StringBuilder(length);
        for (TextRun run : runs) { result.append(run.getText()); }
        return result.toString();
    }

    /**
     * Replaces text and styles after validating the complete list.
     * Empty runs are removed and adjacent runs with same style values are merged.
     * The supplied list is copied, invalid input leaves the content unchanged.
     * @param values replacement text runs.
     * @throws NullPointerException if input is null.
     * @throws ArithmeticException if the combined text length exceeds integer range.
     */
    @Override
    public void setRuns(final List<TextRun> values){
        Objects.requireNonNull(values, "runs");

        List<TextRun> temp = new ArrayList<>();
        int size = 0;
        for (TextRun run : values) {
            Objects.requireNonNull(run, "run");

            size = Math.addExact(size, run.getLength());

            if (run.getLength() == 0) { continue; }
            int last = temp.size() - 1;

            if (last >= 0 && temp.get(last).getStyle().equals(run.getStyle())) {
                TextRun previous = temp.remove(last);
                temp.add(new TextRun(previous.getText() + run.getText(), previous.getStyle()));
            } else {
                temp.add(run);
            }
        }
        runs = List.copyOf(temp);
        length = size;
    }

    /**
     * Copies text and styles into detached graph (no parent).
     * @return an independent parent with no parent.
     */
    @Override
    public Paragraph deepCopy(){return new Paragraph(runs);}

    /**
     * Validates a range without changing content.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @throws IndexOutOfBoundsException if the range is outside the text.
     */
    private void checkBounds(final int start, final int end){
        if (start < 0 || end < start || end > length) {
            throw new IndexOutOfBoundsException("Invalid range");
        }
    }

    /**
     * Validates a nonempty selection.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @throws IndexOutOfBoundsException if the bounds are invalid.
     * @throws IllegalArgumentException if the range is empty.
     */
    private void checkRangeIsNotEmpty(final int start, final int end){
        checkBounds(start, end);
        if (start == end) {
            throw new IllegalArgumentException("Text range must not be empty");
        }
    }

    /**
     * Copies selected range into styled runs without changing paragraph content.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @return the selected text and styles as List.
     * @throws IndexOutOfBoundsException if the range is out of bounds.
     * @throws IllegalArgumentException if the selected range is empty.
     */
    public List<TextRun> slice(final int start, final int end){
        checkRangeIsNotEmpty(start, end);
        return copyRange(start, end);
    }

    /**
     * Builds and replaces segment of text runs with supplied text runs.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @param replacement styled runs to insert between the range defined.
     * @throws IndexOutOfBoundsException if bounds are invalid.
     * @throws NullPointerException if input runs are null.
     */
    private void splice(final int start, final int end, final List<TextRun> replacement){
        checkBounds(start, end);
        List<TextRun> result = new ArrayList<>(copyRange(0, start));
        result.addAll(Objects.requireNonNull(replacement, "replacement must not be null"));
        result.addAll(copyRange(end, length));
        setRuns(result);
    }

    /**
     * Keeps the part of the paragraph stored before position in this Paragraph object and returns the other half as
     * detached paragraph.
     * @param position split index, can range from zero to leght().
     * @return the detached later half of the paragraph.
     * @throws IndexOutOfBoundsException if the split offset is outside the paragraph.
     */
    public Paragraph split(final int position){
        checkBounds(position, position);
        Paragraph suffix = new Paragraph(copyRange(position, length));
        splice(position, length, List.of());
        return suffix;
    }

    /**
     * Appends supplied paragraph. It does not delete or change the supplied paragraph it only appends text and textStyle.
     * @param secondParagraph paragraph whose text and styles are appended.
     * @throws NullPointerException if input paragraph is null.
     * @throws IllegalArgumentException if second paragraph is same paragraph as merged one.
     */
    public void merge(final Paragraph secondParagraph){
        Objects.requireNonNull(secondParagraph, "second paragraph cannot be null");
        if (secondParagraph == this) {
            throw new IllegalArgumentException("Cannot merge a paragraph with itself");
        }
        insertRuns(length, secondParagraph.getRuns());
    }

    /**
     * Copies internal text segment.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @return immutable runs from the segment.
     * @throws IndexOutOfBoundsException if bounds are invalid.
     */
    private List<TextRun> copyRange(final int start, final int end){
        checkBounds(start, end);
        List<TextRun> result = new ArrayList<>();
        int offset = 0;
        for (TextRun run : runs) {
            int left = Math.max(start, offset);
            int right = Math.min(end, offset + run.getLength());
            if (left < right) {
                result.add(new TextRun(run.getText().substring(left - offset, right - offset), run.getStyle()));
            }
            offset += run.getLength();
        }
        return List.copyOf(result);
    }

    /**
     * Insets text at valid position. uses default style.
     * @param position index that can range from zero to length().
     * @param text text to insert.
     * @throws IndexOutOfBoundsException if the range is outside the text.
     * @throws NullPointerException if the input text is null.
     */
    public void insertText(final int position, final String text){}

    /**
     * Insets text at valid position. uses user provided style.
     * @param position index that can range from zero to length().
     * @param text text to insert.
     * @param style immutable style for supplied text.
     * @throws IndexOutOfBoundsException if the range is outside the text.
     * @throws NullPointerException if one of the input is null.
     */
    public void insertText(final int position, final String text, final TextStyle style){}

    /**
     * Deletes a range while preserving styles on the remaining text.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @throws IndexOutOfBoundsException if the range is outside the text or start exceeds end.
     * @throws IllegalArgumentException if the selected range is empty.
     */
    public void deleteText(final int start, final int end){}

    /**
     * Replaces a range with new text. With default style.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @param text text to insert.
     * @throws IndexOutOfBoundsException if the range is outside the text.
     * @throws NullPointerException if a required argument is null.
     * @throws IllegalArgumentException if the selected range is empty.
     */
    public void replaceText(final int start, final int end, final String text){}

    /**
     * Replaces a range with new text. With user defined style.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @param text text to insert.
     * @param style style of the supplied text
     * @throws IndexOutOfBoundsException if the range is outside the text.
     * @throws NullPointerException if a required argument is null.
     * @throws IllegalArgumentException if the selected range is empty.
     */
    public void replaceText(final int start, final int end, final String text, final TextStyle style){}

    /**
     * Replaces range with styled runs.
     * @param start inclusive start index.
     * @param end exclusive end index.
     * @param replacement replacement text and style.
     * @throws IndexOutOfBoundsException if the range is outside the text.
     * @throws NullPointerException if one of the inputs is null.
     * @throws IllegalArgumentException if the selected range is empty.
     */
    public void replaceRuns(final int start, final int end, final List<TextRun> replacement){}

    /**
     * Inserts styled runs at a position, including into an empty paragraph.
     * @param position insertion index from zero through length().
     * @param inserted text runs to insert.
     * @throws IndexOutOfBoundsException if the position is invalid.
     * @throws NullPointerException if the list or a run is null.
     */
    public void insertRuns(final int position, final List<TextRun> inserted){}
}
