package javax0.repl;

import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ReplCompleter implements Completer {
    private Completer completer;

    public ReplCompleter(final List<CommandDefinition> commandDefinitions, final Set<String> aliasNames) {
        setCompleter(commandDefinitions, aliasNames);
    }

    public void setCompleter(final List<CommandDefinition> commandDefinitions, final Set<String> aliasNames) {
        completer = new AggregateCompleter(
            new StringsCompleter(commandDefinitions.stream()
                .flatMap(c -> Optional.ofNullable(c.parameters).stream().flatMap(Collection::stream))
                .collect(Collectors.toSet())),
            new StringsCompleter(commandDefinitions.stream()
                .map(c -> c.keyword.replaceAll("^\\*", "")).collect(Collectors.toSet())),
            new StringsCompleter(aliasNames),
            new Completers.FileNameCompleter()
        );
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        completer.complete(lineReader, parsedLine, list);
    }
}
