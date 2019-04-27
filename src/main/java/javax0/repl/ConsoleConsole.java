package javax0.repl;

import java.io.Console;
import java.io.PrintWriter;

/**
 * {@link LocalConsole} implementation that uses {@link System#console()}
 */

public class ConsoleConsole implements LocalConsole {
    final private Console console = System.console();
    final LocalConsole fallback;

    public ConsoleConsole() {
        fallback = console != null ? null : new BufferedReaderConsole();
    }

    @Override
    public String readLine(String msg) {
        if (console == null) {
            return fallback.readLine(msg);
        } else {
            return console.readLine(msg);
        }
    }

    @Override
    public PrintWriter writer() {
        if (console == null) {
            return fallback.writer();
        } else {
            return console.writer();
        }
    }
}
