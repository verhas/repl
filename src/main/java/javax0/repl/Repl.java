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

    public Repl debug() {
        debugMode = true;
        return this;
    }

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
            final var command = env.parser().get(0);
            if (aliases.containsKey(command.toLowerCase())) {
                w.print(command + " is an alias of " + aliases.get(command.toLowerCase()) + "\n");
                return;
            }
            final var fakeEnv = new ReplCommandEnvironment();
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

    public Repl alias(String alias, String command) {
        aliases.put(alias, command);
        return this;
    }

    public Repl stateReporter(Consumer<CommandEnvironment> stateReporter) {
        this.stateReporter = stateReporter;
        return this;
    }

    public Repl allowExit(Function<CommandEnvironment, Boolean> allowExit) {
        this.allowExit = allowExit;
        return this;
    }

    private void aliasCommand(CommandEnvironment env) {
        final var alias = env.parser().get(0);
        final var command = env.parser().get(1);
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

    public Repl prompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public Repl startup(String startupFile) {
        this.startupFile = startupFile;
        return this;
    }

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
        final var env = new ReplCommandEnvironment();
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
