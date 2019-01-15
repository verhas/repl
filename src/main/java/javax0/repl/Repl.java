package javax0.repl;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.repl.CommandDefinitionBuilder.kw;

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

    public Repl() {
        command(kw("alias").executor(this::aliasCommand).usage("alias myalias command")
                .help("You can freely define aliases for any command.\n" +
                        "You cannot define alias to an alias.")
        ).command(kw("exit").executor(this::exitCommand).usage("exit")
                .help("Use the command 'exit' without parameters to exit from the REPL application")
        ).command(kw("help").executor(this::helpCommand).parameters(Set.of()).usage("help")
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

    public Repl command(CommandDefinitionBuilder builder) {
        final var def = builder.build();
        for( final var cd : commandDefinitions ){
            if( cd.keyword.toLowerCase().equals(def.keyword.toLowerCase())){
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
        LocalConsole console;
        if (System.console() == null) {
            message.warning("No console in the system");
            return new BufferedReaderConsole();
        } else {
            return new ConsoleConsole();
        }
    }

    private void exitCommand(CommandEnvironment env) {
        shouldExit.set(true);
    }

    private void helpCommand(CommandEnvironment env) {
        if (env.parser().get(0) != null) {
            final var command = env.parser().get(0);
            if (aliases.containsKey(command.toLowerCase())) {
                env.console().writer().print(command + " is an alias of " + aliases.get(command.toLowerCase()) + "\n");
                return;
            }
            final var fakeEnv = new ReplCommandEnvironment();
            keywordAndLine(fakeEnv, command);
            final var cd = getCommand(fakeEnv);
            if (cd == null) {
                env.console().writer().print(command + " is unknown");
                return;
            }
            if (cd.help == null) {
                env.console().writer().print("There is no help defined for the command " + cd.keyword + "\n");
            } else {
                env.console().writer().print(cd.help + "\n");
            }
        } else {
            env.console().writer().print("Available commands:\n");
            commandDefinitions.forEach(
                    c -> env.console().writer().print(c.usage + "\n")
            );
            env.console().writer().print("! cmd to execute shell commands\n");
            env.console().writer().print(". filename to execute the content of the file\n");
        }

    }

    public Repl alias(String alias, String command) {
        aliases.put(alias, command);
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
        console.writer().print(fetchMessage());
        if (args != null && args.length > 0) {
            execFile(args[0], console);
            return;
        }
        if (appTitle != null) {
            console.writer().print(appTitle);
        }
        console.writer().print("CDW is " + new File(".").getAbsolutePath() + "\n");
        console.writer().print("type 'help' for help\n");
        console.writer().flush();
        executeStartupFile(console);
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
                console.writer().flush();
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
                console.writer().print("[EXCEPTION] " + e);
            }
            console.writer().print(fetchMessage());
            console.writer().flush();
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
                console.writer().print("Startup file " + startupFile + " was not found");
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
    }

    private CommandDefinition getCommand(ReplCommandEnvironment env) {
        final var commands = commandDefinitions.stream()
                .filter(command -> command.keyword.toLowerCase().startsWith(env.keyword()))
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
