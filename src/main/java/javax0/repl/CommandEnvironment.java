package javax0.repl;

import java.util.regex.Matcher;

public interface CommandEnvironment {
    String keyword();
    String line();
    ParameterParser parser();
    Matcher matcher();
    String matcherId();
    LocalConsole console();
    Message message();

}
