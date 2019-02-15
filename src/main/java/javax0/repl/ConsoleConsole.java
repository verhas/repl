package javax0.repl;

import java.io.Console;
import java.io.PrintWriter;

/**
 * {@link LocalConsole} implementation that uses {@link System#console()}
 */

public class ConsoleConsole implements LocalConsole {
    final private Console console = System.console();

    @Override
    public String readLine(String msg) {
        return console.readLine(msg);
    }

    @Override
    public PrintWriter writer() {
        return console.writer();
    }
}
