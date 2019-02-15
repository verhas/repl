# REPL framework / library

`javax0.repl` is a simple framework to write Read Eval Print Loop type of applications. The library 

* handles the console,
* parses the line the user types in
* invokes the methods that implement the individual commands
* implements commands for `exit`, `alias`, `help`
* implements OS command execution when user starts a line with `!`, e.g.: `! ls -l`
* implements including files like it was typed in when user starts the line with a dot `. file_name`
* reads commands from a startup file and executes them automatically
* provides fluent API to configure the commands of the application using the REPL library

# JavaDoc

For reference the JavaDoc is available at https://verhas.github.io/repl/apidocs/

# Getting started

To start using the library you have to create a simple `public static void main()` that initializes the
structure defining the commands via fluent API and starts the application loop calling `run()` at the end of
the flud API chain. A sample application can be found in the test directories of the applicaton in the 
`javax0.repl.sample` package: `ReplTetsApplicationTest` The application contains the following code:

```java
        sut = new Repl();
        sut.command(
                start().
                        kw("echo")
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
                .stateReporter(this::report)
                .debug()
                .run();
```

The code creates a new `Repl` object that implements the configuration API. In this API the method

* `command()` is used to define a new command
* `alias()` is used to define a command alias
* `title()` defines the title of the application. This will be presented on the console when the application is 
  started.
* `prompt()` defines the prompt the user sees before each line on the console
* `stateReporter()` can specify a `Consumer<CommandEnvironment>` that is supposed to print a message to the console
  about the state of the application. This is a method that has exactly the same interface as any other command
  method, except this is not configured as a command, but rather invoked after every and each command execution.
* `allowExit()` may define a `Function<CommandEnvironment,Boolean>` that will be invoked by the REPL application when
  the user executes the `exit` command.
* `startup()` can be used to define a startup file. This file will be read by the REPL application if it exists and the
  lines will be executed by the application like if they were typed into the command input. This functionality is handy
  to let the user of the REPL application to have his/her own startup file defining aliases or doing some application
  specific state initialization. 
* `debug()` switches on debug mode. In this mode when an exception happens in some of the commands the full exception
  with the stacktrace is printed on the console.
* `run()` starts the console. This method returns only when the user exists the application.

# Command definition

The method `command()` has one argument. This argument has to be supplied using fluent API. It has to be started
with the call to the method `CommandDefinitionBuilder.start()`, which is suggested to be imported static, so that
it can be invoked as in the example: `start()`.

## Keyword

This has to be followed with the keyword definition calling the methof `kw()`. This is a mandatory part of the
call chain. Every command has to have a unique command keyword. This is the keyword that the users should type
at the start of the line. Note that users are required to type in only that many characters as many makes the
command unique, thus there is no reason to be afraid to define long command names.

When a command name is defined with the `*` as the first character then the REPL application will not recognize
the command in abbreviated form. This is to ensure that "dangerous" commands, like `exit` are not executed
accidentally.

##Parameter definition

This is followed by an optional parameter definition. To define the parameters the command may have on the
line one of the API methods `parameter()`, `parameters()` or `noParameters()` can be invoked. The method
`parameter()` can be invoked more than once defining parameters. The method `parameter()` can define a single
parameter by the name of the parameters and it can be invoked multiple times to define more than one parameters.
Alternatively the method `parameters()` (note the plural) can be invoked with a `Set<String>` argument that defines
the parameters for the command.

A command can have parameters and the implementation of the command can access these parameters through a
context object. The parameters have the form `name=value` on the line and the repl application will analize
the command line the user typed in and fills the parameter table ready to be used for the command implementation.
If there are parameters defined then the parsing will raise an error if there is a parameter that is not configured.
If the parameters are configured calling the `noParameters()` method then there are no parameters allowed for the
command. If there is no `parameter()`, `parameters()` or `noParameters()` call configuring the command then the
command can be invoked with any parameter and the command should decide if it can work with the actual parameters
the user typed in.

The definition of the parameters also helps the parsers and the user to identify parameters that are abbreviated.
If the possible parameters are defined then the parser will identify the parameter from a prefix that is already
unique for the specific command but store the value associated to the full name of the parameter. That way
parameter abbreviations are handled automatically and the command implementation does not need to implement any
guesswork to let the users to abbreviate parameters.

## Regex

This method should define a regular expressions that can be matched against the actual command line that follows
the command keyword. The method can be invoked more than once in the fluent chain defining more than one regular
expressions. The regular expressions may also contain groups between `(` and `)`. The REPL will match these
regular expressions one after the other against the command line that the user typed in and in case one of them
matches the line then the command will be able to retrieve the name of the one that matched calling `matcherId()`
and the actual regular expression matcher via `matcher()` on the environment. The matcher can be used to get the
parameters.

If none of the regular expressions match then the command is treated as syntactically incorrect, an error is
displayed by the REPL application and the command is not invoked. 

## Usage and help

The next two methods to be invoked are `usage()` adn `help()`. Both methods are mandatory and have to be invoked in
this order. The arguments define the usage and help strings that the REPL application prints in response to the
`help` command the user types in. The string defined calling the method `usage()` is printed when the 
user types `help` without argument. This will form a summary of all available commands. When the user types
the name of a command after the keyword `help` then the implementation of the help command will print the
help text of that command as defined in the argument to the invokcation of the configuring `help()` method.

## Executor

Themethod `executor()` should define a `Consumer` that consumes a `CommandEnvironment`. The command environment
interface defines methods that can be used to access the actual command keyword, the whole command line, the
parameters, the regular expression match result and so on. The actual application should implement only these
command methods.

# Built-in commands

The REPL application implements some commands that are the same for all REPL applications. These are the followings:

* `help` to display help
* `exit` to exit the application
* `.` (dot) to execute commands from a file like the lines of the file were typed by the user
* `!` (exclamation mark) to execute shell commands (except `cd`)
* `alias` to define aliases for commands

## `help` showing help

The command help will display the usage strings of all the commands or in case the user provides an argument, which
is the name of the command possibly abbreviated then it will display the help text of that command.

## `exit` the application

Invoking this command will return to the `public static void main()` method, essentially exiting the application.
Since REPL applications may store some state than may need saving the REPL application invokes the
`Function<CommandEnvironment,Boolean>` defined as an argument to the method `allowExit()` in the setup of the
REPL application. If this function is not defined or defined and returns `true` then the command `exit` will
be executed. If the function is defined and returns false then a warning message is displayed and the command
execution does not finish, the application does not finish.

The warning message will inform the user to use the parameter `confirm=yes` to force the exit. If this parameter is
used, like

```
exit confirm=yes
```

then the program will exit. The configured function is invoked even if the user uses the `confirm=yes` parameter
this way making it possible to save the state for some implementations.

## `.` execute from a file

The dot command is literally a `.` dot character that can have one argument, the name of a file. This file will be read
by the REPL application and the commands will be executed. The usual feedback messages are not displayed for each
command as they are executed from the file. If there is an `exit` command in the file (should not, it is bad practice)
then exiting will only be performed at the end of the execution of the file. Commands read from file cannot execute
shell commands and cannot include other files.

## `!` shell

if the command line starts with the `!` exclamation mark then the rest of the line is passed to the underlying
operating system shell to execute. This functionality was tested on Windows and on Unix like operating systems
including OSX. This way the user can execute simple shell commands like `ls` or `dir` or rename, move around files.

The user can not change the current working directory. Java does not provide functionality to change the current
working directory of the Java process and when `cd` is executed via the shell it changes the current working directory
of the freshly started shell process that is terminated immediately after the command was executes and thus `cd` is 
of no use.

## `alias` definition

Aliases can be defined during the startup of the application but there is also a command in the REPL application,
`alias` available for the users of the application to define aliases. The syntax of the command is 

```
alias myalias command
```

After executing this command `myalias` can be used instead of `command`. When using aliases on the command line
they cannot be abbreviated unlike the commands themselves. If the command is executed without a `command`, like

```
alias myalias
```

then the alias becomes undefined.

It is a good practice to let the users define a startup file and to include their own aliases instead of hardwiring 
aliases. The commands and startup files or files included using the dot command can any time redefine or delete any
alias.

# Command development

## Example 1 with regular expression

Commands are implemented as methods. The sample application in the `test` directory contains a command that calculates
the absolute value of a complex number. We will have a look at this code as a simple example. This example uses the
regular expression parsing. The code of this command is the following:

```java
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
```
The command is defined as

```java
kw("abs")
.regex("canonical", "(\\d+)\\s*\\+(\\d+)i")
.regex("polar", "(\\d+)\\((\\d+\\.?\\d*)\\)")
.usage("abs complexnumber")
.help("Print out the absolut value of a complex number\n" +
        "You can specify the complex number in a+bi format or\n" +
        "R(rad) format.")
.executor(this::absCommand)
```

you can see that instead of parameters the command defines two regular expressions. One of the expressions should
match the command after the `abs` keyword.

Note that the keyword is not part of the matching string, because it
is what it alredy is and known to the command code. On the other hand the user can use any alias for the command
and thus there is no reason to overcomplicate the regular expression.

The two regular expressions are named `polar` and `canonical`. When the command is invoked the name of the expression
that was matching is provided by the call `env.matcherId()`. The `Matcher` object itself is available through the call
`env.matcher()` and on this object the method `get(X)` provides the strings that were macthed by the regular expression
groups enclosed between `(` and `)`.

The output is printed to the console as information message calling `info()` on the message object returned by
`env.message()`. 

## Example 1 with parameters

Another sample command is `return`. This command prints out a text if the parameter `output` is `yes`. The output
may be delayed with the parameter `delay`. The code of the command is the following:

```java
private void returnCommand(CommandEnvironment env) {
    final var delay = env.parser().get("delayed");
    if (delay.isPresent()) {
        try {
            Thread.sleep(Integer.parseInt(delay.get()));
        } catch (InterruptedException ignored) {
        }
    }
    final var output = env.parser().get("output", Set.of("yes", "no"));
    if (output.isPresent() && output.get().equals("yes")) {
        final var text = env.parser().getOrDefault("text","");
        env.message().info(text);
    }
}
```

The command is defined as

```java
kw("return")
.parameter("output").parameter("delayed").parameter("text")
.usage("return value")
.help("Use return to calculate a value and return it to the console.")
.executor(this::returnCommand)
```

As you can see there are three parameters configured for the command: `output`, `delayed` and `text`. The parameter
`delayed` can have any value. If it is not well formatted then the parsing will throw and exception but the command
itself does not need to care about that. The repl application will catch the exception and display it for the user.

The parameter `output` can have only two values. These can be `yes` and `no`. When the value of the parameter is
queried these possible values are given in a `Set`. If the parameter has a different value, which is not present in 
the set then the parsing will throw an exception and the REPL application will handle that. The command code
can rely on the returned value that it is nothing else but one of the expected string. The user benefits from this
in the way that the parsing allows for the user to abbreviate these values. In case of our example the `output=yes`
can also be written as `o=y` as an extremely short form. 

## Command Environment

The command gets a `CommandEnvironment` object. This object can be used to get access to the actual command as the
use typed it (it is rarely needed), the rest of the line, the parameters, the matchers in case of regular expression
matching and the concole to output as well as the message object to collect info, warning and error messages. For more
information please read the JavaDoc documentation of the class `CommandEnvironment` 

# Documentation

You, as a developer of a REPL application want to document your application. This documentation will include the
text describing the commands. Since the built-in commands provided by the library are not special for the users
your documentation should include the description of these commands. To eace this task the 

https://github.com/verhas/repl/blob/master/BUILTINS.md

file contains in markdown format the documentation for these commands. You can edit and paste this text into your
documentation.






 