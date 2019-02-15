Note: this documentation file is here to help the documentation of the actual REPL application, which uses this
library. Eventually such an application want to document the commands it supports. This documentation can be
extended copying the content of this file to include the built-in commands without writing their documentation
again and again. 

1. Search every occurence of `REPL$ >` in the text and replace it with your applications prompt.
1. Copy the changed text into your documentation!

# General commands

The application implements general commands. These are the followings:

* `help` to display help
* `exit` to exit the application
* `.` (dot) to execute commands from a file like the lines of the file were typed by the user
* `!` (exclamation mark) to execute shell commands (except `cd`)
* `alias` to define aliases for commands

## `help` showing help

The command help will display the usage strings of all the commands or in case you provide an argument, which
is the name of the command then it will display the help text of that command.

In the application just type:

```
REPL$ > help
```

to get general help of

```
REPL$ > help exit
```

to get detailed help on the command `exit`. You can also get help on any other command. You can also use
command names that are aliases. In this case the help text will tell you what command the alias stands for and in
case you need help on that command you can provide the name of that command. You can also abbreviate the name
of the command in case the command name is abbreviatable. Note that some commands, like `exit` can not be abbreviated
for safety reason.

## `exit` the application

Use this command to exit the application, like

```
REPL$ > exit
```

It may happen that the application has some unsaved values. In this case the command will give you a warning and will
not exit. This lets you to execute the commands that save the data. If you want to force exit you have to use the
command in the format 

```
exit confirm=yes
```

then the program will exit.

## `.` execute from a file

The dot command is literally a `.` dot character that can have one argument, the name of a file. This file will be read
by the application and the commands will be executed.

## `!` shell

If the command starts with the `!` exclamation mark then the rest of the line is passed to the underlying
operating system shell to execute. This functionality works on Windows and on Unix like operating systems
including OSX. This way you can execute simple shell commands like `ls` or `dir` or rename, move around files.

You cannot change the current working directory.

## `alias` definition

This command can define an alias for some command. The syntax of the command is 

```
alias myalias command
```

After executing this command `myalias` can be used instead of `command`. When using aliases on the command line
they cannot be abbreviated unlike the commands themselves. If the command is executed without a `command`, like

```
alias myalias
```

then the alias becomes undefined.

