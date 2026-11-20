package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.model.Document;

import java.util.List;

/** Replaces the first matching occurrence in document paragraphs. */
public final class ReplaceFirstCommand implements EditCommand {

    private final Document document;
    private final SearchService search;
    private final String query;
    private final String replacement;
    private final MatchMode mode;

    private ReplaceMatchCommand replacementCommand;
    private boolean executed;

    /**
     * Creates a replace-first command.
     *
     * @param document the document to edit
     * @param search the search service
     * @param query the text to find
     * @param replacement the replacement text; may be empty
     * @param mode substring or whole-word matching
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if query is empty
     */
    public ReplaceFirstCommand(
            Document document,
            SearchService search,
            String query,
            String replacement,
            MatchMode mode) {

        if (document == null || search == null) {

            throw new NullPointerException("Document and search service cannot be null.");

        }

        if (query == null || replacement == null || mode == null) {

            throw new NullPointerException("Search arguments cannot be null.");

        }

        if (query.isEmpty()) {

            throw new IllegalArgumentException("Search text cannot be empty.");

        }

        this.document = document;
        this.search = search;
        this.query = query;
        this.replacement = replacement;
        this.mode = mode;
    }

    /**
     * Replaces the first matching occurrence in a paragraph or section title.
     *
     * @throws IllegalStateException if already executed
     * @throws IllegalArgumentException if no matches exist
     */
    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException(
                    "Replacement is already executed.");
        }

        if (replacementCommand == null) {
            List<SearchMatch> matches =
                    search.find(document, query, mode);

            if (matches.isEmpty()) {
                throw new IllegalArgumentException(
                        "No matches found.");
            }

            SearchMatch match = matches.get(0);

            replacementCommand =
                    new ReplaceMatchCommand(match, replacement);
        }

        replacementCommand.execute();
        executed = true;
    }

    /**
     * Restores the original matched text and its formatting.
     *
     * @throws IllegalStateException if the command is not executed
     */
    @Override
    public void undo() {
        if (executed == false) {

            throw new IllegalStateException("Execute the replacement before undoing it.");

        }

        replacementCommand.undo();
        executed = false;
    }
}