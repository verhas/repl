package javax0.repl;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class CommandDefinition {
    final String keyword;
    final Set<String> parameters;
    final Map<String, Pattern> regexes;
    final Consumer<CommandEnvironment> executor;
    final String usage;
    final String help;

    private CommandDefinition(String keyword,
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

    public static class Builder {
        private String keyword;
        private Set<String> parameters;
        private Consumer<CommandEnvironment> executor;
        private Map<String, Pattern> regexes;
        private String usage;
        private String help;

        public static Builder kw(String keyword) {
            final var it = new Builder();
            it.keyword = keyword;
            return it;
        }

        public CommandDefinition build() {
            return new CommandDefinition(keyword, parameters, executor, regexes, usage, help);
        }

        public Builder executor(Consumer<CommandEnvironment> executor) {
            this.executor = executor;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder help(String help) {
            this.help = help;
            return this;
        }

        public Builder parameters(Set<String> parameters) {
            if (this.parameters == null) {
                this.parameters = new HashSet<>(parameters);
            } else {
                this.parameters.addAll(parameters);
            }
            return this;
        }

        public Builder noParameters() {
            if (parameters == null) {
                this.parameters = new HashSet<>(Set.of());
            } else {
                throw new IllegalArgumentException(
                        "You cannot define parameters and noParameters for the same command");
            }
            return this;
        }

        public Builder parameter(String parameter) {
            if (parameters == null) {
                this.parameters = new HashSet<>(Set.of(parameter));
            } else {
                this.parameters.add(parameter);
            }
            return this;
        }

        public Builder regex(String name, String regex) {
            if (regexes == null) {
                this.regexes = new HashMap<>();
            }
            regexes.put(name,Pattern.compile(regex));
            return this;
        }
    }

}
