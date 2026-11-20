import java.util.ArrayList;

public class Macro {
    public String name;
    public ArrayList<String> commandLines;


    public Macro() {
        name = "";
        commandLines = new ArrayList<String>();
    }

    public Macro(String name) {
        this.name = name;
        commandLines = new ArrayList<String>();
    }

    public Macro(String name, ArrayList<String> commands) {
        this.name = name;
        commandLines = commands;
    }

    public void addCommand(String command) {
        commandLines.add(command);
    }

    public ArrayList<String> getCommands() {
        return commandLines;
    }
}

