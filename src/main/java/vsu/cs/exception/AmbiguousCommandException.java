package vsu.cs.exception;

import java.util.List;

public class AmbiguousCommandException extends RuntimeException {
    private final String input;
    private final List<String> matches;

    public AmbiguousCommandException(String input, List<String> matches) {
        super("Ambiguous command: " + input);
        this.input = input;
        this.matches = matches;
    }

    public String getInput() { return input; }
    public List<String> getMatches() { return matches; }
}
