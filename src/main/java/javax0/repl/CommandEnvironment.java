package javax0.repl;

import java.util.regex.Matcher;

public interface CommandEnvironment {
    /**
     * @return the keyword that was typed on the command line. If the user used an abbreviated form or an alias the
     * returned string will be the abbreviated for or the alias itself and not the defined command name.
     */
    String keyword();

    /**
     * @return the part of the command line that comes after the user typed command name
     */
    String line();

    /**
     * @return the parameter parser that can be used to retrieve the parameters
     */
    ParameterParser parser();

    /**
     * @return the matcher that was matching the line (the part of the line returned by the {@link #line()} method.
     * In case there was no regular expression matching the returned value is null.
     */
    Matcher matcher();

    /**
     * @return the name of the regular expression that was matched. This name is given as a string in the command
     * definition.
     */
    String matcherId();

    /**
     * @return the console that can be used by the command to print output. Command should not use any other means to
     * to directly output values to the user.
     */
    LocalConsole console();

    /**
     * @return the message object that can be used to collect info, warning and error messagaes. These messages are
     * displayed to the user when the command has finished.
     */
    Message message();
}
