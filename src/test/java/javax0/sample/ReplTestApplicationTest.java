package javax0.sample;

import javax0.repl.CommandEnvironment;
import javax0.repl.Repl;

import static javax0.repl.CommandDefinitionBuilder.start;

class ReplTestApplicationTest {

    public static void main(String[] args) {
        new ReplTestApplicationTest().test();
    }

    private Repl sut;

    private void test() {
        sut = new Repl();
        sut.command(
            start().
                kw("echo")
                .noParameters()
                .usage("echo parameters")
                .help("Use echo to print out to the console the parameters that are given on the line")
                .executor(this::echoCommand)
        ).alias("e", "echo")
            .command(
                start()
                    .kw("return")
                    .parameter("immediate").parameter("delayed").parameter("format")
                    .usage("return value")
                    .help("Use return to calculate a value and return it to the console.")
                    .executor(this::returnCommand)
            ).alias("ret", "return").alias("a", "alias")
            .command(start().
                kw("abs")
                .regex("canonical", "(\\d+)\\s*\\+(\\d+)i")
                .regex("polar", "(\\d+)\\((\\d+\\.?\\d*)\\)")
                .usage("abs complexnumber")
                .help("Print out the absolut value of a complex number\n" +
                    "You can specify the complex number in a+bi format or\n" +
                    "R(rad) format.")
                .executor(this::absCommand)
            )
            .command(start().kw("alias").usage("").help("").executor(this::myAlias))
            .title("Sample REPL Application to end-to-end manual test the application")
            .prompt("REPL > $ ")
            .debug()
            .run()
        ;
    }

    private void absCommand(CommandEnvironment env) {
        if (env.matcherId().equals("polar")) {
            final var abs = Integer.parseInt(env.matcher().group(1));
            env.message().info("" + abs);
        } else {
            final var real = Integer.parseInt(env.matcher().group(1));
            final var imag = Integer.parseInt(env.matcher().group(2));
            final var abs = Math.sqrt(real * real + imag * imag);
            env.message().info("" + abs);
        }
    }

    private void myAlias(CommandEnvironment env) {
        env.console().writer().print("This is my alias!!!\n");
        env.console().writer().flush();
        final var alias = env.parser().get(0);
        final var command = env.parser().get(1);
        sut.alias(alias, command);
        env.message().info(alias + " was really set to alias " + command);
    }

    private void returnCommand(CommandEnvironment env) {

    }

    private void echoCommand(CommandEnvironment env) {
        for (int i = 0; env.parser().get(i) != null; i++) {
            env.message().info(env.parser().get(i));
        }
    }

}
