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
* `exit` to //TODO
 