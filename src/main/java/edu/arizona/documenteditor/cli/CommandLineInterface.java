package edu.arizona.documenteditor.cli;

import java.util.Scanner;

/** Reads complete console commands and displays their results. */
public final class CommandLineInterface {
    private final CommandDispatcher dispatcher;
    private final Scanner input;

    /**
     * Creates an interface reading standard input.
     * @param dispatcher shared dispatcher
     * @throws NullPointerException if dispatcher is null
     */
    public CommandLineInterface(CommandDispatcher dispatcher) {
        this(dispatcher, new Scanner(System.in));
    }

    /**
     * Creates an interface with a supplied input source for tests.
     * @param dispatcher shared dispatcher
     * @param input source of complete command lines
     * @throws NullPointerException if either argument is null
     */
    public CommandLineInterface(CommandDispatcher dispatcher, Scanner input) {
        if (dispatcher == null || input == null) {

            throw new NullPointerException("Dispatcher and input cannot be null.");
        }
        this.dispatcher = dispatcher;
        this.input = input;
    }

    /** Reads until exit or end of input; leaves the input stream open. */
    public void run() {

        System.out.println("Type help for commands, or exit to quit.");
        while (true) {

            System.out.print("> ");
            if (!input.hasNextLine()) {

                break;
            }
            String line = input.nextLine();

            if (line.trim().equalsIgnoreCase("exit")) {

                break;
            }
            if (!line.isBlank() && !line.stripLeading().startsWith("#")) {

                display(dispatcher.dispatch(line));
            }
        }
    }

    /**
     * Displays a result.
     * @param result result to display
     * @throws NullPointerException if result is null
     */
    public void display(CommandResult result) {
        if (result == null) {
            throw new NullPointerException("Result cannot be null.");
        }
        if (result.isSuccess()) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("Error: " + result.getMessage());
        }
    }
}
