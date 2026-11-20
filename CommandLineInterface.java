import java.util.Scanner;


// CommandLineInterface: Reads the console, uses what it reads and gives it to CommandParser
public class CommandLineInterface {

    CommandDispatcher dispatcher;

    public CommandLineInterface() {
        dispatcher = new CommandDispatcher();
    }
    public void run() {
        //Takes what  input the user puts in at console(?) and stores to a string.
        Scanner readInput = new Scanner(System.in);
        String InputLine = readInput.nextLine();
        dispatcher.parser.parse(InputLine);
    }

    public void runScript(String Path) {
        //Honestly idk how to work this thing.
    }

    /*
    public void display(CommandResult result) {


    }
     */
}
