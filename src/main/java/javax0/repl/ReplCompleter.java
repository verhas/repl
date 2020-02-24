package javax0.repl;

import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A jline {@link Completer} object that contains all the strings that can be used to auto complete the actual command
 * line. The list contains all the commands, all the parameters to all the commands, and the file name complementer is
 * also added.
 */
class ReplCompleter implements Completer {
    private Completer commandCompleter;
    final List<CommandDefinition> commandDefinitions;

    public ReplCompleter(final List<CommandDefinition> commandDefinitions, final Set<String> aliasNames) {
        this.commandDefinitions = commandDefinitions;
        setCompleter(commandDefinitions, aliasNames);
    }

    public void setCompleter(final List<CommandDefinition> commandDefinitions, final Set<String> aliasNames) {
        commandCompleter = new AggregateCompleter(
            // all the commands
            new StringsCompleter(commandDefinitions.stream()
                // keywords starting with '*' are not abbreviated, but
                // still can be used in auto-complete w/o the '*' of course
                .map(c -> c.keyword.replaceAll("^\\*", ""))
                .collect(Collectors.toSet())),
            new StringsCompleter(aliasNames),
            new Completers.FileNameCompleter()
        );

    }

    private final static CommandDefinition EMPTY_COMMAND_DEFINITION = new CommandDefinition(
        null, Set.of(), null, null, null, null);

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        final Completer actualCompleter;
        final String line = parsedLine.line();
        if (line.contains(" ")) {
            actualCompleter = getCommandSpecificCompleter(line);
        } else {
            actualCompleter = commandCompleter;
        }
        actualCompleter.complete(lineReader, parsedLine, list);
    }

    private Map<String, Completer> completerMap = new HashMap<>();

    private Completer getCommandSpecificCompleter(String line) {
        Completer actualCompleter;
        final String keyword = line.substring(0, line.indexOf(' '));
        if (completerMap.containsKey(keyword)) {
            return completerMap.get(keyword);
        }
        CommandDefinition commandDefinition =
            commandDefinitions.stream().filter(cd -> cd.keyword.equals(keyword)).findFirst()
                .orElse(EMPTY_COMMAND_DEFINITION);
        actualCompleter = new AggregateCompleter(
            new StringsCompleter(commandDefinition.parameters),
            new Completers.FileNameCompleter()
        );
        completerMap.put(keyword, actualCompleter);
        return actualCompleter;
    }
}
