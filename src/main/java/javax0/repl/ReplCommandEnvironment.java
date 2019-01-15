package javax0.repl;

import java.util.regex.Matcher;

class ReplCommandEnvironment implements CommandEnvironment {
    ParameterParser parser;
    String keyword;
    String line;
    Matcher matcher;
    String matcherId;
    LocalConsole console;
    Message message;

    @Override
    public String keyword() {
        return keyword;
    }

    @Override
    public String line() {
        return line;
    }

    @Override
    public ParameterParser parser() {
        return parser;
    }

    @Override
    public Matcher matcher() {
        return matcher;
    }

    @Override
    public String matcherId() {
        return matcherId;
    }

    @Override
    public LocalConsole console() {
        return console;
    }

    @Override
    public Message message() {
        return message;
    }
}
