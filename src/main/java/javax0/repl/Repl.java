package javax0.repl;

import javax0.repl.CommandDefinitionBuilder.CommandDefinitionBuilderReady;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.repl.CommandDefinitionBuilder.start;

public class Repl implements Runnable {
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private final Map<String, String> aliases = new HashMap<>();
    private final AtomicBoolean shouldExit = new AtomicBoolean(false);
    private final List<CommandDefinition> commandDefinitions = new ArrayList<>();
    private String[] args;
    private Message message = new Message();
    private String appTitle;
    private String startupFile;
    private String prompt = "$ ";
    private boolean debugMode = false;
    private Consumer<CommandEnvironment> stateReporter;
    private Function<CommandEnvironment, Boolean> allowExit;

    /**
     * Create a new object that already has the built-in commands configured.
     */
    public Repl() {
        command(start().kw("alias")
            .usage("alias myalias command")
            .help("You can freely define aliases for any command.\n" +
                "You cannot define alias to an alias.")
            .executor(this::aliasCommand)
        ).command(start().kw("*exit") // it starts with '*', user cannot abbreviate
            .parameter("confirm")
            .usage("exit")
            .help("Use the command 'exit' without parameters to exit from the REPL application")
            .executor(this::exitCommand)
        ).command(start().kw("help")
            .parameters(Set.of())
            .usage("help")
            .help("")
            .executor(this::helpCommand)
        );
    }

    private static Process getOsProcessObject(String s) throws IOException {
        final Process process;
        if (isWindows) {
            process = Runtime.getRuntime()
                .exec(String.format("cmd.exe /c %s", s));
        } else {
            process = Runtime.getRuntime()
                .exec(String.format("sh -c %s", s));
        }
        return process;
    }

    private void shell(String s, LocalConsole console) {
        try {
            if (s.startsWith("cd ")) {
                console.writer().print("[ERROR] you can not change the working directory\n");
                return;
            }
            final Process process = getOsProcessObject(s);
            final var sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            console.writer().print("[SHELL OUTPUT]\n" + sb.toString() + "[END SHELL OUTPUT]\n");
        } catch (IOException e) {
            if (debugMode) {
                e.printStackTrace();
            }
            console.writer().print("[EXCEPTION] " + e + "\n");
        }
    }

    /**
     * Switch the Repl into debug mode. In this mode the exceptions are printed to the screen including the stack trace.
     * In non-debug mode only a short information is printed about exceptions that happen during command execution but
     * the code details are not displayed.
     * @return this
     */
    public Repl debug() {
        debugMode = true;
        return this;
    }

    /**
     * Define a command. The command has to be specified in the form of a builder which is implemented in the
     * {@link CommandDefinitionBuilder} and which implements fluent API to ease the definition of commands.
     * Note that the final {@link CommandDefinitionBuilderReady#build()} method should not be invoked. It will
     * be invoked here by the method.
     * @param builder the builder that is ready for the building.
     * @return this
     */
    public Repl command(CommandDefinitionBuilderReady builder) {
        final var def = builder.build();
        for (final var cd : commandDefinitions) {
            if (cd.keyword.toLowerCase().equals(def.keyword.toLowerCase())) {
                commandDefinitions.remove(cd);
                break;
            }
        }
        commandDefinitions.add(def);
        return this;
    }

    /**
     * Pass the arguments to the application from the OS command line.
     *
     * @param args the arguments that the application should see as it was passed on the command line by the user as
     *             the repl application was started
     * @return this
     */
    public Repl args(String[] args) {
        this.args = args;
        return this;
    }

    /**
     * Fetch the messages that were accumulated since the message was fetched last time.
     *
     * @return the string of the messages
     */
    private String fetchMessage() {
        final var msg = message.message();
        message = new Message();
        return msg;
    }

    private LocalConsole getConsole() {
        if (System.console() == null) {
            message.warning("No console in the system");
            return new BufferedReaderConsole();
        } else {
            return new ConsoleConsole();
        }
    }

    private void exitCommand(CommandEnvironment env) {
        if (allowExit != null && !allowExit.apply(env)) {
            if (env.parser().get("confirm", Set.of("yes")) != null) {
                shouldExit.set(true);
            } else {
                env.message().warning("There is unsaved state in the application. Use 'exit confirm=yes'");
            }
        } else {
            shouldExit.set(true);
        }
    }

    private void helpCommand(CommandEnvironment env) {
        final var w = env.console().writer();
        if (env.parser().get(0) != null) {
            final var command = env.parser().get(0).orElse(null);
            if (aliases.containsKey(command.toLowerCase())) {
                w.print(command + " is an alias of " + aliases.get(command.toLowerCase()) + "\n");
                return;
            }
            final var fakeEnv = new ReplCommandEnvironment(this);
            keywordAndLine(fakeEnv, command);
            final var cd = getCommand(fakeEnv);
            if (cd == null) {
                w.print(command + " is unknown");
                return;
            }
            if (cd.help == null) {
                w.print("There is no help defined for the command " + cd.keyword + "\n");
            } else {
                w.print(cd.usage + "\n");
                w.print(cd.help + "\n");
            }
        } else {
            w.print("Available commands:\n");
            commandDefinitions.forEach(
                c -> w.print(c.usage + "\n")
            );
            w.print("! cmd to execute shell commands\n");
            w.print(". filename to execute the content of the file\n");
            if (!aliases.isEmpty()) {
                w.print("Aliases:\n");
                aliases.keySet().forEach(
                    s -> w.print(s + " -> " + aliases.get(s) + "\n")
                );
            }
        }
        w.flush();
    }

    /**
     * Define a new alias
     * @param alias the name of the alias the user can later use
     * @param command the command that the alias means
     * @return this
     */
    public Repl alias(String alias, String command) {
        if (command == null) {
            aliases.remove(alias);
        } else {
            aliases.put(alias, command);
        }
        return this;
    }

    /**
     * Define a command, which is started after each configured command execution. This command can use the environment
     * console to output information about the state of the application.
     * @param stateReporter the command implementation
     * @return this
     */
    public Repl stateReporter(Consumer<CommandEnvironment> stateReporter) {
        this.stateReporter = stateReporter;
        return this;
    }

    /**
     * Define an allow exit function. The allow exit function is consulted by the repl application when the user types
     * {@code exit}. A {@code true} value from the function will signal the application that there is no problem exiting
     * from the application. The function should veto the exiting in case there is some unsaved data. The function
     * may also save the data and then signal {@code true}. If the return value of the function is {@code false} then
     * the application will not exit unless the user specified the {@code confirm=yes} parameter. If this parameter
     * is specified the function is still consulted but its veto (a.k.a. the returned {@code false} value) is ignored
     * and the application will exit.
     * @param allowExit the function that implements the unsaved resource checking and vetoing
     * @return this
     */
    public Repl allowExit(Function<CommandEnvironment, Boolean> allowExit) {
        this.allowExit = allowExit;
        return this;
    }

    private void aliasCommand(CommandEnvironment env) {
        final var alias = env.parser().get(0).orElse(null);
        final var command = env.parser().get(1).orElse(null);
        alias(alias, command);
        env.message().info(alias + " was set to alias " + command);
    }

    private void execFile(String fileName, LocalConsole console) {
        message.info("Executing '" + fileName + "'");
        try (final var reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    try {
                        execute(line, console);
                    } catch (Exception e) {
                        message.error("" + e);
                    }
                    console.writer().print(fetchMessage());
                    console.writer().flush();
                }
            }
        } catch (Exception e) {
            message.error("" + e);
            console.writer().print(fetchMessage());
        }
    }

    /**
     * Provide a title for the application. It will be printed when the application starts.
     *
     * @param title the tile that is printed
     * @return this
     */
    public Repl title(String title) {
        appTitle = title;
        return this;
    }

    /**
     * Define the prompt that th euser will see in the
     * @param prompt
     * @return
     */
    public Repl prompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Define the name of the startup file that is executed when the applicatoin starts to run.
     * @param startupFile the name of the file
     * @return this
     */
    public Repl startup(String startupFile) {
        this.startupFile = startupFile;
        return this;
    }

    /**
     * Run the application.
     */
    public void run() {
        final LocalConsole console = getConsole();
        final var w = console.writer();
        w.print(fetchMessage());
        if (args != null && args.length > 0) {
            execFile(args[0], console);
            return;
        }
        if (appTitle != null) {
            w.print(appTitle);
        }
        w.print("\nCDW is " + new File(".").getAbsolutePath() + "\n");
        w.print("type 'help' for help\n");
        executeStartupFile(console);
        w.print(fetchMessage());
        w.flush();
        for (; ; ) {
            final var rawLine = console.readLine(prompt);
            if (rawLine == null) {
                return;
            }
            final var line = rawLine.trim();
            if (line.equalsIgnoreCase("")) {
                continue;
            }

            if (line.trim().startsWith(".")) {
                execFile(line.trim().substring(1).stripLeading(), console);
                continue;
            }
            if (line.startsWith("!")) {
                shell(line.substring(1), console);
                w.flush();
                continue;
            }
            try {
                execute(line, console);
            } catch (IllegalArgumentException e) {
                message.error(e.getMessage());
            } catch (Exception e) {
                if (debugMode) {
                    e.printStackTrace();
                }
                w.print("[EXCEPTION] " + e);
            }
            w.print(fetchMessage());
            w.flush();
            if (shouldExit.get()) {
                return;
            }
        }

    }

    private void executeStartupFile(LocalConsole console) {
        if (startupFile != null) {
            final var file = new File(startupFile);
            if (file.exists()) {
                console.writer().print("Executing startup file " + file.getAbsolutePath());
                execFile(file.getAbsolutePath(), console);
            } else {
                console.writer().print("Startup file " + startupFile + " was not found.\n");
            }
        }
    }

    private void execute(String line, LocalConsole console) {
        final var env = new ReplCommandEnvironment(this);
        env.message = message;
        final String trimmedLine = line.trim();
        if (trimmedLine.length() == 0) {
            return;
        }
        keywordAndLine(env, trimmedLine);
        seekAlias(env);
        final var cd = getCommand(env);
        if (cd == null) {
            return;
        }
        env.console = console;
        env.parser = parseLine(env, cd.parameters);
        if (matchRegexes(env, cd.regexes)) {
            cd.executor.accept(env);
        } else {
            message.error("None of the syntax patterns could match the line. See the help of the command.");
        }
        if (stateReporter != null) {
            stateReporter.accept(env);
        }
    }

    private boolean kwMatch(CommandDefinition cd, String kw) {
        if (cd.keyword.startsWith("*")) {
            return cd.keyword.substring(1).toLowerCase().equals(kw);
        }
        return cd.keyword.toLowerCase().startsWith(kw);
    }

    private CommandDefinition getCommand(ReplCommandEnvironment env) {
        final var kw = env.keyword().toLowerCase();
        final var commands = commandDefinitions.stream()
            .filter(command -> kwMatch(command, kw))
            .collect(Collectors.toList());
        if (commands.size() > 1) {
            message.error("command '" + env.keyword() + "' is ambiguous");
            return null;
        }
        if (commands.size() == 0) {
            message.error("command '" + env.keyword() + "' is not defined");
            return null;
        }
        return commands.get(0);
    }

    private ParameterParser parseLine(CommandEnvironment env, Set<String> parameters) {
        return ParameterParser.parse(env.line(), parameters);
    }

    private boolean matchRegexes(ReplCommandEnvironment env, Map<String, Pattern> regexes) {
        if (regexes != null) {
            for (final var entry : regexes.entrySet()) {
                final var matcher = entry.getValue().matcher(env.line());
                if (matcher.matches()) {
                    env.matcher = matcher;
                    env.matcherId = entry.getKey();
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void seekAlias(ReplCommandEnvironment env) {
        env.keyword = aliases.getOrDefault(env.keyword, env.keyword);
    }

    private void keywordAndLine(ReplCommandEnvironment env, String line) {
        final var spaceIndex = line.indexOf(" ");
        if (spaceIndex == -1) {
            env.keyword = line;
        } else {
            env.keyword = line.substring(0, spaceIndex);
        }
        env.line = line.substring(env.keyword.length()).trim();
    }

}
