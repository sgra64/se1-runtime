package runtime;

import java.util.List;

/**
 * Original Spring Boot code: CommandLineRunner.java
 * https://github.com/spring-projects/spring-boot/blob/main/core/spring-boot/src/main/java/org/springframework/boot/CommandLineRunner.java
 */
public interface CommandRunner {

    record CommandRunnerInstance(
        CommandRunner runner,
        String cmd,
        KVArgs kvargs
    ) {
        void run() { runner.run(cmd, kvargs); }
    }

    interface KVArgs {

        String value(String key, String... altValue);

        int asInt(String key, Integer... altValue);

        double asDouble(String key, Double... altValue);

        boolean asBoolean(String key, Boolean... altValue);

        List<String> asList(String key);

        List<Integer> asIntList(String key);

        boolean hasKey(String key);
    }

    static List<CommandRunnerInstance> create(CommandRunner runner, String commands, String[] args) {
        return runtime.impl.RuntimeSystem.getInstance().createCommandRunnerInstances(runner, commands, args);
    }

    static void run(CommandRunner runner, String commands, String[] args) {
        for(var cr : create(runner, commands, args)) {
            cr.run();
        }
    }

    void run(String command, KVArgs args);
}
