package javax0.repl;

import javax0.geci.annotations.Geci;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Geci("fluent definedBy='javax0.repl.CommandDefinitionBuilderFluenter::sourceBuilderGrammar'")
public class CommandDefinitionBuilder {
    private String keyword;
    private Set<String> parameters;
    private Consumer<CommandEnvironment> executor;
    private Map<String, Pattern> regexes;
    private String usage;
    private String help;

    public CommandDefinitionBuilder kw(String keyword) {
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
        regexes.put(name, Pattern.compile(regex));
        return this;
    }

    //<editor-fold id="fluent" desc="fluent API interfaces and classes">
    public static If10 start(){
        return new Wrapper();
    }
    public static class Wrapper implements If0,If2,If1,If4,If3,If6,If5,If8,If7,If9,If10{
        private final javax0.repl.CommandDefinitionBuilder that;
        public Wrapper(){
            this.that = new javax0.repl.CommandDefinitionBuilder();
        }
        public Wrapper usage(String arg1){
            that.usage(arg1);
            return this;
        }
        public Wrapper help(String arg1){
            that.help(arg1);
            return this;
        }
        public Wrapper noParameters(){
            that.noParameters();
            return this;
        }
        public Wrapper build(){
            that.build();
            return this;
        }
        public Wrapper kw(String arg1){
            that.kw(arg1);
            return this;
        }
        public javax0.repl.CommandDefinitionBuilder executor(java.util.function.Consumer<javax0.repl.CommandEnvironment> arg1){
            return that.executor(arg1);
        }
        public Wrapper regex(String arg1, String arg2){
            that.regex(arg1,arg2);
            return this;
        }
        public Wrapper parameters(java.util.Set<String> arg1){
            that.parameters(arg1);
            return this;
        }
        public Wrapper parameter(String arg1){
            that.parameter(arg1);
            return this;
        }
    }
    public interface If0 {
        javax0.repl.CommandDefinitionBuilder executor(java.util.function.Consumer<javax0.repl.CommandEnvironment> arg1);
    }
    public interface If1 extends If0 {
        If0 help(String arg1);
    }
    public interface If2 extends If1 {
        If1 usage(String arg1);
    }
    public interface If3 extends If2 {
        If2 regex(String arg1, String arg2);
    }
    public interface If5 {
        If3 noParameters();
    }
    public interface If6 {
        If3 parameters(java.util.Set<String> arg1);
    }
    public interface If7 extends If3 {
        If7 parameter(String arg1);
    }
    public interface If8 {
        If7 parameter(String arg1);
    }
    public interface If9 extends If6,If5,If8{
    }
    public interface If4 extends If3,If9 {}
    public interface If10 {
        If4 kw(String arg1);
    }
    //</editor-fold>
}
