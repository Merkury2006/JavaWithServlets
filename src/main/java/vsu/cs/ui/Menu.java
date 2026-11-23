package vsu.cs.ui;

import vsu.cs.exception.AmbiguousCommandException;
import vsu.cs.exception.UnknownCommandException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Menu {
    private final ConsoleIO io;
    private final String title;
    private final Map<String, MenuItem> items = new LinkedHashMap<>();

    private String exitCommand = "back";
    private String exitLabel = "Back";

    public Menu(ConsoleIO io, String title) {
        this.io = io;
        this.title = title;
    }

    public Menu add(String command, String label, Runnable action) {
        String key = command.toLowerCase();

        if (items.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate menu command: " + key);
        }

        items.put(command.toLowerCase(), new MenuItem(label, action));
        return this;
    }

    public Menu exit(String command, String label) {
        this.exitCommand = command.toLowerCase();
        this.exitLabel = label;
        return this;
    }

    public void run() {
        boolean running = true;
        while (running) {
            this.print();
            String input = io.readLine("> ").trim().toLowerCase();

            if (input.isEmpty()) {
                System.out.println("Empty command.");
                continue;
            }

            if (exitCommand.startsWith(input)) {
                running = false;
                continue;
            }

            try {
                MenuItem item = this.resolve(input);
                io.safe(item.action());
            } catch (AmbiguousCommandException e) {
                System.out.println("Ambiguous command '" + e.getInput() + "'. Matches: " + e.getMatches());
            } catch (UnknownCommandException e) {
                System.out.println("Unknown command: " + e.getInput());
            }
        }
    }

    private MenuItem resolve(String input) {
        MenuItem exact = items.get(input);

        if (exact != null) {
            return exact;
        }

        List<Map.Entry<String, MenuItem>> matches = items.entrySet().stream()
                .filter(e -> e.getKey().startsWith(input))
        .toList();

        if (matches.size() == 1) {
            return matches.getFirst().getValue();
        }

        if (matches.size() > 1) {
            throw new AmbiguousCommandException(input, matches.stream().map(Map.Entry::getKey).toList());
        }
        throw new UnknownCommandException(input);
    }

    private void print() {
        StringBuilder sb = new StringBuilder("\n--- ").append(title).append(" ---\n");
        items.forEach((key, value) -> sb.append(String.format("%-15s %s%n", key, value.label())));
        sb.append(String.format("%-15s %s", exitCommand, exitLabel));
        System.out.println(sb);
    }

    private record MenuItem(String label, Runnable action) {}
}
