package javax0.repl;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * An object that holds all the information that defines a command.
 */
class CommandDefinition {
    final String keyword;
    final Set<String> parameters;
    final Map<String, Pattern> regexes;
    final Consumer<CommandEnvironment> executor;
    final String usage;
    final String help;

    CommandDefinition(String keyword,
                              Set<String> parameters,
                              Consumer<CommandEnvironment> executor,
                              Map<String, Pattern> regexes,
                              String usage,
                              String help) {
        this.keyword = keyword;
        this.parameters = parameters;
        this.executor = executor;
        this.regexes = regexes;
        this.usage = usage;
        this.help = help;
    }
}
