package javax0.repl;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public class JLineConsole implements LocalConsole {
    private final Terminal terminal;
    private final LocalConsole fallback;
    private final LineReader reader;

    public JLineConsole(List<CommandDefinition> commandDefinitions, Set<String> aliasNames) {
        terminal = getTerminal();
        reader = terminal == null ? null : LineReaderBuilder.builder().completer(new ReplCompleter(commandDefinitions,aliasNames)).terminal(terminal).build();
        fallback = terminal != null ? null : new BufferedReaderConsole();
    }

    private static Terminal getTerminal() {
        try {
            return TerminalBuilder.terminal();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String readLine(String msg) {
        if (reader == null) {
            return fallback.readLine(msg);
        } else {
            return reader.readLine(msg);
        }
    }

    @Override
    public PrintWriter writer() {
        if (terminal == null) {
            return fallback.writer();
        } else {
            return terminal.writer();
        }
    }
}
