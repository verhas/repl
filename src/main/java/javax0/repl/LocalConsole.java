package javax0.repl;

import java.io.PrintWriter;

public interface LocalConsole {
    String readLine(String msg);
    PrintWriter writer();
}
