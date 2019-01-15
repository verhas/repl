package javax0.repl;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<String> infos = new ArrayList<>();

    public void error(String s) {
        errors.add(s);
    }

    public void warning(String s) {
        warnings.add(s);
    }

    public void info(String s) {
        infos.add(s);
    }

    public String message() {
        final var sb = new StringBuilder();
        errors.forEach(s -> sb.append("[ERROR] ").append(s).append("\n"));
        warnings.forEach(s -> sb.append("[WARNING] ").append(s).append("\n"));
        infos.forEach(s -> sb.append("[INFO] ").append(s).append("\n"));
        return sb.toString();
    }

}
