package vsu.cs.exception;

public class UnknownCommandException extends RuntimeException {
    private final String input;

    public UnknownCommandException(String input) {
        super("Unknown command: " + input);
        this.input = input;
    }

    public String getInput() { return input; }
}
