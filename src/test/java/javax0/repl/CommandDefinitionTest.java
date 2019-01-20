package javax0.repl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

class CommandDefinitionTest {

    @Test
    @DisplayName("Test the fluent API of command definition")
    void testCommandDefinition() {
        /*
        kw("echo").executor(this::echoCommand).usage("echo parameters")
                .help("Use echo to print out to the console the parameters that are given on the line")
                .noParameters().build();
        kw("return").executor(this::returnCommand).usage("return value")
                .help("Use return to calculate a value and return it to the console.")
                .parameter("immediate").parameter("delayed").parameter("format").build();
        kw("return").executor(this::returnCommand).usage("return value")
                .help("Use return to calculate a value and return it to the console.")
                .parameters(Set.of("immediate", "delayed", "format")).build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> kw("return")
                .parameters(Set.of("immediate", "delayed", "format")).noParameters());
        kw("return").executor(this::returnCommand).usage("return value")
                .help("Use return to calculate a value and return it to the console.")
                .parameters(Set.of("immediate", "delayed")).parameters(Set.of("format")).build();
        kw("abs").executor(this::absCommand)
                .regex("canonical", "(\\d+)\\s*\\+(\\d+)i")
                .regex("polar", "(\\d+)\\((\\d+\\.?\\d*)\\)")
                .usage("abs complexnumber")
                .help("Print out the absolut value of a complex number\n" +
                        "You can specify the complex number in a+bi format or\n" +
                        "R(rad) format.").build();
                        */

    }

    private void absCommand(CommandEnvironment env) {
    }

    private void returnCommand(CommandEnvironment env) {

    }

    private void echoCommand(CommandEnvironment env) {
    }
}
