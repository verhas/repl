package javax0.repl;

import java.util.Set;

import static javax0.repl.CommandDefinitionBuilder.start;

/**
 * A sample application that defines no real commands at all. This is to manually test the Repl, especially command
 * completion.
 */
public class Noop {
    public static void main(String[] args) {
        new Noop().noop();
    }

    private void noop() {
        final var sut = new Repl();
        sut.title("Noop REPL Application to end-to-end manual test the application")
            .prompt("REPL > $ ")
            .debug()
            .command(
                start()
                    .kw("first")
                    .parameters(Set.<String>of("f1", "f2"))
                    .usage("")
                    .help("")
                    .executor(this::noop))
            .command(
                start()
                    .kw("second")
                    .parameters(Set.<String>of("s1", "s2"))
                    .usage("")
                    .help("")
                    .executor(this::noop))
            .command(
                start()
                    .kw("third")
                    .parameters(Set.<String>of("t1", "t2"))
                    .usage("")
                    .help("")
                    .executor(this::noop))
            .alias("3rd", "third")
            .run()
        ;
    }

    private void noop(CommandEnvironment env) {
        env.message().info(env.line());
    }
}
