package runtime;

import java.util.List;

/**
 * Interface {@link CommandRunner} defines the API of a utility that can split
 * command line arguments by key-words (<i>"commands"</i>) into several
 * invocations of the {@code run(String command, KVArgs args)} method on a
 * single instance of a class that implements the {@link CommandRunner} interface.
 * <p>
 * Invocations are represented as {@link CommandRunnerInstance} instances.
 * <p>
 * Each invocation receives pre-processed arguments as a {@link KVArgs} object.
 * <p>
 * The {@link CommandRunner} utility was adapted from
 * <a href="https://spring.io/projects/spring-boot"><i>Spring Boot's</i></a>
 * <a href="https://github.com/spring-projects/spring-boot/blob/main/core/spring-boot/src/main/java/org/springframework/boot/CommandLineRunner.java">
 *      <i>CommandLineRunner.java</i></a> interface.
 * @version <code style=color:green>{@value runtime.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtime.package_info#Author}</code>
 */
public interface CommandRunner {

    /**
     * Record {@link CommandRunnerInstance} represents one invocation of the
     * {@code run(String command, KVArgs args)} method on a single instance of a
     * class that implements the {@link CommandRunner} interface.
     */
    record CommandRunnerInstance(

        /** The command runner instance on which the command run is invoked. */
        CommandRunner runner,

        /** The command string. */
        String cmd,

        /** The key-value arguments for the command. */
        KVArgs kvargs
    ) {
        /**
         * Invoke the command run passing the command and key-value arguments.
         */
        void run() { runner.run(cmd, kvargs); }
    }

    /**
     * Interface {@link KVArgs} provides access to typed key-value arguments
     * converted from bare {@code String[] args} command line arguments.
     * <p>
     * Examples:
     * <pre>
     * x = 100          // named parameter "x" with value 100
     * y = 3.14         // named parameter "y" with value 3.14
     * z = true         // named parameter "z" with value true
     * list = [a,b,c]   // named parameter "list" with String value ["a", "b", "c"]
     * list = [1,2,3]   // named parameter "list" with int value [1, 2, 3]
     * drop, --no-drop  // examples of named parameters without values (key-only)
     * </pre>
     */
    interface KVArgs {

        /**
         * Return value associated with the key as a string. If no value is
         * present, return the optional alternative value or {@code null}.
         * @param key named parameter key to look up.
         * @param altValue optional alternative value if no value is present.
         * @return the value associated with the key or the alternative value.
         */
        String value(String key, String... altValue);

        /**
         * Return value associated with the key as an integer. If no value is
         * present, return the optional alternative value or {@code 0}.
         * @param key named parameter key to look up.
         * @param altValue optional alternative value if no value is present.
         * @return the value associated with the key or the alternative value.
         */
        int asInt(String key, Integer... altValue);

        /**
         * Return value associated with the key as a double. If no value is
         * present, return the optional alternative value or {@code 0.0}.
         * @param key named parameter key to look up.
         * @param altValue optional alternative value if no value is present.
         * @return the value associated with the key or the alternative value.
         */
        double asDouble(String key, Double... altValue);

        /**
         * Return value associated with the key as a boolean. If no value is
         * present, return the optional alternative value or {@code false}.
         * @param key named parameter key to look up.
         * @param altValue optional alternative value if no value is present.
         * @return the value associated with the key or the alternative value.
         */
        boolean asBoolean(String key, Boolean... altValue);

        /**
         * Return value associated with the key as a list of strings or an
         * empty list {@code []}.
         * @param key named parameter key to look up.
         * @return the value associated with the key or the alternative value.
         */
        List<String> asList(String key);

        /**
         * Return value associated with the key as a list of integers or an
         * empty list {@code []}.
         * @param key named parameter key to look up.
         * @return the value associated with the key or the alternative value.
         */
        List<Integer> asIntList(String key);

        /**
         * Return if the given key is present.
         * @param key named parameter key to look up.
         * @return true if the key is present, false otherwise.
         */
        boolean hasKey(String key);
    }

    /**
     * Return a list of command runner instances by splitting raw {@code args[]}
     * by comma-separated words (<i>"commands"</i>) in the {@code commands}
     * argument.
     * @param runner the command runner instance on which the command run is performed.
     * @param commands comma-separated words to split {@code args[]}.
     * @param args raw command line arguments to split and convert into key-value
     * arguments for each command.
     * @return list of command runner instances.
     */
    static List<CommandRunnerInstance> create(CommandRunner runner, String commands, String[] args) {
        return runtime.impl.RuntimeSystem.getInstance().createCommandRunnerInstances(runner, commands, args);
    }

    /**
     * Split and run command runner instances by invoking method
     * {@code run(String command, KVArgs args)} on each created instance.
     * @param runner the command runner instance on which the command run is performed.
     * @param commands comma-separated words to split {@code args[]}.
     * @param args raw command line arguments.
     */
    static void run(CommandRunner runner, String commands, String[] args) {
        for(var cr : create(runner, commands, args)) {
            cr.run();
        }
    }

    /**
     * Method invoked for a command with key-value arguments.
     * @param command the command string.
     * @param args the key-value arguments for the command.
     */
    void run(String command, KVArgs args);
}
