import java.util.Objects;
import java.util.Scanner;


// CommandLineInterface: Reads the console, uses what it reads and gives it to CommandParser
public class CommandLineInterface {

    CommandDispatcher dispatcher;

    public CommandLineInterface() {
        dispatcher = new CommandDispatcher();
    }
    public CommandLineInterface(CommandDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    public void run() {
        CommandResult result;
        boolean ifContinue = true;
        //Takes what  input the user puts in at console(?) and stores to a string.

        // While loop continues until user inputs continue in the command prompt, in which it will end the while loop and thus the interface.
        while (ifContinue) {
            // Display Prompt
            System.out.println("Enter command: ");

            // Read users input
            Scanner readInput = new Scanner(System.in);
            String InputLine = readInput.nextLine();
            if (Objects.equals(InputLine, "exit")) {
                ifContinue = false;
                continue;
            }
            // Sends users input to CommandDispatcher/dispatcher's parser
            ParsedCommand newParser = dispatcher.parser.parse(InputLine);

            if (newParser != null) {
                result = new CommandResult(true, "Command Parsed correctly.");
            } else {
                result = new CommandResult(false, "ERROR: Failed to parse command.");
            }

            this.display(result);
        }
    }
    /*
    public void runScript(String Path) {
        //RunScript version, ScriptRunner is using this version instead
    }
    */

    public void display(CommandResult result) {
        String message = result.getMessage();
        boolean isSuccess = result.isSuccess();

        System.out.println("Message: " + message);
        System.out.println("Validation: " + isSuccess);


    }

}
