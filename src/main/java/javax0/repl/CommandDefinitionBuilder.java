package javax0.repl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class CommandDefinitionBuilder {
    private String keyword;
    private Set<String> parameters;
    private Consumer<CommandEnvironment> executor;
    private Map<String, Pattern> regexes;
    private String usage;
    private String help;

    public static CommandDefinitionBuilder kw(String keyword) {
        final var it = new CommandDefinitionBuilder();
        it.keyword = keyword;
        return it;
    }

    public CommandDefinition build() {
        return new CommandDefinition(keyword, parameters, executor, regexes, usage, help);
    }

    public CommandDefinitionBuilder executor(Consumer<CommandEnvironment> executor) {
        this.executor = executor;
        return this;
    }

    public CommandDefinitionBuilder usage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandDefinitionBuilder help(String help) {
        this.help = help;
        return this;
    }

    public CommandDefinitionBuilder parameters(Set<String> parameters) {
        if (this.parameters == null) {
            this.parameters = new HashSet<>(parameters);
        } else {
            this.parameters.addAll(parameters);
        }
        return this;
    }

    public CommandDefinitionBuilder noParameters() {
        if (parameters == null) {
            this.parameters = new HashSet<>(Set.of());
        } else {
            throw new IllegalArgumentException(
                    "You cannot define parameters and noParameters for the same command");
        }
        return this;
    }

    public CommandDefinitionBuilder parameter(String parameter) {
        if (parameters == null) {
            this.parameters = new HashSet<>(Set.of(parameter));
        } else {
            this.parameters.add(parameter);
        }
        return this;
    }

    public CommandDefinitionBuilder regex(String name, String regex) {
        if (regexes == null) {
            this.regexes = new HashMap<>();
        }
        regexes.put(name,Pattern.compile(regex));
        return this;
    }
}
