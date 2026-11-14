package edu.arizona.documenteditor.model;


import java.util.List;

public final class Paragraph implements DocumentNode,FormatTarget {
    Section parent;
    private List<TextRun> runs = List.of();
    private int length;
    public Paragraph(){}
    public Paragraph(final String text){}
    public Paragraph(final String text, final TextStyle style){}
    public Paragraph(final List<TextRun> initialRuns){}
    @Override
    public Section getParent(){}
    @Override
    public int length(){}
    @Override
    public List<TextRun> getRuns(){}
    @Override
    public String getText(){}
    @Override
    public void setRuns(final List<TextRun> values){}
    @Override
    public Paragraph deepCopy(){}
    private void checkBounds(final int start, final int end){}
    private void checkRange(final int start, final int end){}
    public List<TextRun> slice(final int start, final int end){}
    private void splice(final int start, final int end, final List<TextRun> replacement){}
    public Paragraph split(final int position){}
    public void merge(final Paragraph secondParagraph){}
    private List<TextRun> copyRange(final int start, final int end){}
    public void insertText(final int position, final String text){}
    public void insertText(final int position, final String text, final TextStyle style){}
    public void deleteText(final int start, final int end){}
    public void replaceText(final int start, final int end, final String text){}
    public void replaceText(final int start, final int end, final String text, final TextStyle style){}
    public void replaceRuns(final int start, final int end, final List<TextRun> replacement){}
    public void insertRuns(final int position, final List<TextRun> inserted){}
}
