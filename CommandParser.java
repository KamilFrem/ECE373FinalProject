import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class CommandParser {

    // Produces a ParseCommand(?)
    public ParsedCommand parse(String line) {


        //Enter procedure on how the line would be used to make a ParsedCommand()
        // Use split() to split the spacing of the lines into an actual list

        String[] lineCommand = line.split(" ", 2);

        int contentIndexOne = line.indexOf("\"");
        //Gets content starting from first " to end of the second ". Example: "Hello Word", content is Hello World.
        String Content = line.substring(contentIndexOne, line.indexOf("\"",contentIndexOne+1));
        // ParsedCommand consist of: Command, Target, Content.

        // Combines Command and Target from lineCommand and content of the value between quote marks
        ArrayList<String> newArgument = new ArrayList<>(Arrays.asList(lineCommand));
        newArgument.add(Content);

        //Checks if the Command being inputted is part of the available operations in the word editor

        // NOTE: Unknown how to specify Paragraph and Section types in Word Editor.
        if (!(Objects.equals(newArgument.get(0), "Insert") || Objects.equals(newArgument.get(0), "Delete")
                || Objects.equals(newArgument.get(0), "Replace") || Objects.equals(newArgument.get(0), "Move"))
                || Objects.equals(newArgument.get(0), "Undo") || Objects.equals(newArgument.get(0), "Redo")) {
            return null;
        }
        else if (!(Objects.equals(newArgument.get(0) + " " + newArgument.get(1), "Split Paragraph")
                || Objects.equals(newArgument.get(0) + " " + newArgument.get(1), "Merge Paragraphs"))) {
            return null;
        }

        //Checks if the target is being inputted is part of the available targets in word editor.
        if (!(Objects.equals(newArgument.get(0), "Undo") || Objects.equals(newArgument.get(0), "Redo"))) {
            if (!(Objects.equals(newArgument.get(1), "Paragraph") || Objects.equals(newArgument.get(1), "Section")
                    || Objects.equals(newArgument.get(1), "Paragraphs"))) {
                return null;
            }
        }

        ParsedCommand newCommand = new ParsedCommand(newArgument.get(0) + " " + newArgument.get(1), newArgument);

        return newCommand;
    }

}
