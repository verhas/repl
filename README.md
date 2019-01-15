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