import java.util.ArrayList;

public class ParsedCommand {

    public String name;
    public ArrayList<String> arguments;

    public ParsedCommand() {
        name = "null";
        arguments = new ArrayList<String>();
    }
    public ParsedCommand(ArrayList<String> arguments) {
        name = "Unknown";
        this.arguments = arguments;
    }
    public ParsedCommand(String name) {
        this.name = name;
        arguments = new ArrayList<String>();
    }
    public ParsedCommand(String name,ArrayList<String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
    public String getName() {
        return name;
    }
    public ArrayList<String> getArguments() {
        return arguments;
    }

}
