public class CommandResult {

    private boolean success;
    private String message;
    public CommandResult() {
        success = true;
        message = "Message was successful(?)";
    }
    public CommandResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
