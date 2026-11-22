package edu.arizona.documenteditor.cli;

/** Stores the success flag and display message for one request. */
public final class CommandResult {
    private final boolean success;
    private final String message;

    /**
     * Creates a result.
     * @param success whether the request succeeded
     * @param message text to display
     * @throws NullPointerException if message is null
     */
    public CommandResult(boolean success, String message) {
        if (message == null) {
            throw new NullPointerException("Message cannot be null.");
        }
        this.success = success;
        this.message = message;
    }

    /** @return whether the request succeeded */
    public boolean isSuccess() {
        return success;
    }

    /** @return the display message */
    public String getMessage() {
        return message;
    }
}
