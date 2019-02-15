package javax0.repl;

/**
 * A sample application that defines no commands at all. If this is started only the built-in commands of the Repl
 * are available.
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
                .run()
        ;
    }

}
