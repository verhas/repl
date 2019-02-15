package javax0.repl;

import java.util.*;

public class ParameterParser {

    private final Map<String, String> keys = new HashMap<>();
    private final List<String> values = new ArrayList<>();

    /**
     * Parse a string and build up the parsed structures.
     * <p>
     * A line can have the form
     * <p>
     * {@code
     * key1=value1 key2=value2 .... parameter ... key3=value3 ... parameter
     * }
     * <p>
     * The keys have to be unique, so no one key can be used twice. The parameters that stand alone on the line
     * without an associated key can be mixed between, before and after the keys.
     * <p>
     * The keys, in case they are defined in the argument {@code parameters} are case insensitive.
     *
     * @param line       the line that contains the keys and also the arguments
     * @param parameters a set of parameter names that are allowed on the line. In case this parameter is null any
     *                   parameter is allowed. If this set is empty then no parameter is allowed. If the set contains
     *                   the parameter names then the line may use any non-ambiguous prefix of any parameter and the
     *                   parsed structure will contain the full parameter name even if the user typed a short prefix.
     * @return the parsed structure object that can later be queried
     * @throws IllegalArgumentException if the line is not properly formatted
     */
    static ParameterParser parse(String line, Set<String> parameters) {
        final var it = new ParameterParser();
        final var parts = line.split("\\s+");
        for (final var part : parts) {
            final var eq = part.indexOf("=");
            if (eq == -1) {
                if (part.length() > 0) {
                    it.values.add(part);
                }
            } else {
                final var key = part.substring(0, eq);
                final var value = part.substring(eq + 1);
                it.keys.put(findIt(key, parameters), value);
            }
        }
        return it;
    }

    private static String findIt(String prefix, Set<String> set) {
        if (set == null) {
            return prefix;
        }
        final List<String> commandsFound = new ArrayList<>();
        for (final var s : set) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                commandsFound.add(s);
            }
        }
        if (commandsFound.size() == 1) {
            return commandsFound.get(0);
        }
        if (commandsFound.size() == 0) {
            throw new IllegalArgumentException(prefix + " is not an allowed parameter");
        }
        throw new IllegalArgumentException("Parameter " + prefix + " is ambiguous. " +
                "It matches " + String.join(",", commandsFound) + ".");
    }

    /**
     * Get the value associated with the key using a set of predefined values. The actual value that was parsed
     * from the line may be an unambiguous prefix of the possible values. In that case the value listed in the set
     * will be the one that is defined in the set. If the value cannot be found in the set
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param key    for which we want to retrieve the value
     * @param values the possible values
     * @return the optional value found from the set or {@code Optional.empty()} if the parameter
     * was not present on the command
     */
    public Optional<String> get(String key, Set<String> values) {
        if (get(key).isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(findIt(get(key).get(), values));
    }

    /**
     * Get the value that was associated with the key on the parsed line.
     *
     * @param key the full key as it was defined in the possible keys set
     * @return the optional value as it was on the line or {@code Optional.empty()}
     * in case the key was not present on the line
     */
    public Optional<String> get(String key) {
        return Optional.ofNullable(keys.get(key));
    }

    /**
     * Get the {@code i}-th parameter (starting with 0) from the parsed line. In case {@code i} is larger than
     * the index of the last parameter {@code Optional.empty()} is returned.
     *
     * @param i index of the parameter
     * @return the parameter from the line or {@code Optional.empty()} if {@code i} is too large
     */
    public Optional<String> get(int i) {
        if (i < values.size()) {
            return Optional.ofNullable(values.get(i));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the value associated with the key or the default in case the key was not present on the command line.
     *
     * @param key we look for
     * @param def the default value for the key
     * @return the value for the key
     */
    public String getOrDefault(String key, String def) {
        return keys.getOrDefault(key, def);
    }

    /**
     * The same as {@link #get(String, Set)} but if the key was not defined it will return the default value
     * instead of {@code null}.
     *
     * @param key    for which we want to retrieve the value
     * @param def    the default value to be returned in case the key was not present on the line
     * @param values the possible values
     * @return the value for the key
     */
    public String getOrDefault(String key, String def, Set<String> values) {
        final var value = get(key);
        if (value.isEmpty()) {
            return def;
        }
        return findIt(value.get(), values);
    }
}
