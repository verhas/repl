package javax0.repl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static javax0.repl.CommandDefinitionBuilder.start;

@SuppressWarnings("EmptyMethod")
class CommandDefinitionTest {

    @Test
    @DisplayName("Test the fluent API of command definition")
    void testCommandDefinition() {
        start().
            kw("echo")
            .noParameters()
            .usage("echo parameters")
            .help("Use echo to print out to the console the parameters that are given on the line")
            .executor(this::echoCommand).build();
        start().
            kw("return")
            .parameter("immediate")
            .parameter("delayed")
            .parameter("format")
            .usage("return value")
            .help("Use return to calculate a value and return it to the console.")
            .executor(this::returnCommand)
            .build();
        start().
            kw("return")
            .parameters(Set.of("immediate", "delayed", "format"))
            .usage("return value")
            .help("Use return to calculate a value and return it to the console.")
            .executor(this::returnCommand)
            .build();
        start().
            kw("return")
            .parameters(Set.of("immediate", "delayed", "format"))
            .usage("return value")
            .help("Use return to calculate a value and return it to the console.")
            .executor(this::returnCommand)
            .build();
        start().
            kw("abs")
            .regex("canonical", "(\\d+)\\s*\\+(\\d+)i")
            .regex("polar", "(\\d+)\\((\\d+\\.?\\d*)\\)")
            .usage("abs complexnumber")
            .help("Print out the absolut value of a complex number\n" +
                "You can specify the complex number in a+bi format or\n" +
                "R(rad) format.")
            .executor(this::absCommand)
            .build();

    }

    private void absCommand(CommandEnvironment env) {
    }

    private void returnCommand(CommandEnvironment env) {

    }

    private void echoCommand(CommandEnvironment env) {
    }
}
