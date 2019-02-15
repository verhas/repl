package javax0.repl;

import java.io.PrintWriter;

/**
 * This is a local interface that defines a subset of the functionalities of the system console. This functionality
 * is always available for commands through the {@link CommandEnvironment#console()}.
 *
 * There are two implementations of this interface in this project. One uses the system console and the code
 * uses that implementation when the console is available. The other one uses System.in and out when the console
 * is not available. This is the case when the application is stated from an environment that is not the OS
 * command line.
 */
public interface LocalConsole {
    /**
     * Read a line from the console. This is not to be used by the commands only in case of special situations.
     * @param msg the message printed to the consule as a special prompt.
     * @return the line read
     */
    String readLine(String msg);

    /**
     * @return the print writer to the console.
     */
    PrintWriter writer();
}
