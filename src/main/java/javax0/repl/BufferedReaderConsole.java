package javax0.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * {@link LocalConsole} implementation that uses {@link System#in} and {@link System#out}
 */
public class BufferedReaderConsole implements LocalConsole {
    private final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
    private final PrintWriter writer = new PrintWriter(System.out);
    @Override
    public PrintWriter writer() {
        return writer;
    }
    @Override
    public String readLine(String msg) {
        try {
            System.out.print(msg);
            return reader.readLine();
        } catch (IOException e) {
            writer.print("Cannot read from standard input...\nNo more fallback");
            e.printStackTrace(System.err);
        }
        return null;
    }

}
