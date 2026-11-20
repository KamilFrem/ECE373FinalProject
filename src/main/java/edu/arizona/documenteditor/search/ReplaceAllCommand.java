package edu.arizona.documenteditor.search;

import edu.arizona.documenteditor.command.CompositeCommand;
import edu.arizona.documenteditor.command.EditCommand;
import edu.arizona.documenteditor.model.Document;

import java.util.ArrayList;
import java.util.List;

/** Replaces all matching occurrences in document paragraphs. */
public final class ReplaceAllCommand implements EditCommand {

    private final Document document;
    private final SearchService search;
    private final String query;
    private final String replacement;
    private final MatchMode mode;

    private CompositeCommand replacements;
    private boolean executed;

    /**
     * Creates a replace-all command.
     *
     * @param document the document to edit
     * @param search the search service
     * @param query the text to find
     * @param replacement the replacement text; may be empty
     * @param mode substring or whole-word matching
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if query is empty
     */
    public ReplaceAllCommand(
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
     *  Replaces all matching occurrences in paragraphs and section titles.
     * Each replacement uses the style of its first matched character.
     *
     * @throws IllegalStateException if already executed
     * @throws IllegalArgumentException if no matches exist
     */
    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException(
                    "Replacements are already executed.");
        }

        if (replacements == null) {
            List<SearchMatch> matches =
                    search.find(document, query, mode);

            if (matches.isEmpty()) {
                throw new IllegalArgumentException(
                        "No matches found.");
            }

            List<EditCommand> commands = new ArrayList<EditCommand>();

            // Replace backward so earlier match positions remain valid.
            for (int i = matches.size() - 1; i >= 0; i--) {
                SearchMatch match = matches.get(i);

                ReplaceMatchCommand command =
                        new ReplaceMatchCommand(match, replacement);

                commands.add(command);
            }

            replacements = new CompositeCommand(commands);
        }

        replacements.execute();
        executed = true;
    }

    /**
     * Restores all replaced text and its original formatting.
     *
     * @throws IllegalStateException if the command is not executed
     */
    @Override
    public void undo() {
        if (executed == false) {

            throw new IllegalStateException("Execute the replacements before undoing them.");

        }

        replacements.undo();
        executed = false;
    }
}