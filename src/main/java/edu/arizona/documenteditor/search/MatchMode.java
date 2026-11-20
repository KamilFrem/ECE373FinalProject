package edu.arizona.documenteditor.search;

/** Defines whether search matches substrings or complete words. */
public enum MatchMode {

    /** Matches text anywhere, including inside a larger word. */
    SUBSTRING,

    /** Matches text only at word boundaries. */
    WHOLE_WORD
}