package javax0.sample;

import javax0.repl.CommandEnvironment;
import javax0.repl.Repl;

import static javax0.repl.CommandDefinition.Builder.kw;

class ReplTestApplication {

    public static void main(String[] args) {
        new ReplTestApplication().test();
    }

    private void test() {
        final var sut = new Repl();
        sut
                .command(kw("echo").executor(this::echoCommand).usage("echo parameters")
                        .help("Use echo to print out to the console the parameters that are given on the line")
                        .noParameters()
                ).alias("e", "echo")
                .command(kw("return").executor(this::returnCommand).usage("return value")
                        .help("Use return to calculate a value and return it to the console.")
                        .parameter("immediate").parameter("delayed").parameter("format")
                ).alias("ret", "return").alias("a", "alias")
                .command(kw("abs").executor(this::absCommand)
                        .regex("canonical", "(\\d+)\\s*\\+(\\d+)i")
                        .regex("polar", "(\\d+)\\((\\d+\\.?\\d*)\\)")
                        .usage("abs complexnumber")
                        .help("Print out the absolut value of a complex number\n" +
                                "You can specify the complex number in a+bi format or\n" +
                                "R(rad) format.")

                ).debug()
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

    private void returnCommand(CommandEnvironment env) {

    }

    private void echoCommand(CommandEnvironment env) {
        for (int i = 0; env.parser().get(i) != null; i++) {
            env.message().info(env.parser().get(i));
        }
    }

}
