# Debug

To start the appication in the command line in debug mode so that the jline functioning can be
tested use the following command line:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=\*:5005 -cp target/classes:$HOME/.m2/repository/org/jline/jline/3.13.3/jline-3.13.3.jar javax0.repl.Noop
```

