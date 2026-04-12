package runtime.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import runtime.CommandRunner;
import runtime.CommandRunner.CommandRunnerInstance;
import runtime.Logger;
import runtime.SE1_Runtime;
import runtime.Runner;

/**
 * Class {@link RuntimeSystem} provides the access point to non-public
 * implementation classes of the package {@code runtime.impl}.
 * @version <code style=color:green>{@value runtime.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtime.package_info#Author}</code>
 */
public final class RuntimeSystem implements SE1_Runtime {

    protected final static String LoggerName = "Runtime";

    private final static Logger log = Logger.getLogger(LoggerName);

    private static RuntimeSystem runtimeSystem = null;

    private RuntimeBuilder runtimeBuilder = null;


    /**
     * Classes found during class scan at startup.
     */
    private final List<Class<?>> classes = new ArrayList<>();

    /**
     * Subset of runnable classes assignable from the {@link CommandRunner}
     * interface, sorted by the {@link CommandRunnerImpl.Accessors} priority.
     */
    private final List<Class<Runner>> runnableClasses = new ArrayList<>();
    private final List<Runner> runnableInstances = new ArrayList<>();
    private final PropertiesImpl properties = new PropertiesImpl();

    private RuntimeSystem() {
        log.info(String.format("Running on Java %s", System.getProperty("java.version")));
        log.info(String.format("%s, singleton instance created", this.getClass().getSimpleName()));
    }

    /**
     * Return the singleton instance of the runtime system. Create the instance
     * when it does not yet exist (lazy initialization).
     * @return the singleton instance of the runtime system.
     */
    public static RuntimeSystem getInstance() {
        return Optional.ofNullable(runtimeSystem).orElseGet(() -> runtimeSystem = new RuntimeSystem());
    }

    /**
     * Return named logger instance. Create when name is not yet preset.
     * @param name logger name
     * @return named logger
     */
    public static Logger getLogger(String name) { return LoggerImpl.getLogger(name); }

    /**
     * Create command runner instances for the given command runner, commands and command line arguments.
     * @param runner the command runner instance on which the command run is performed.
     * @param commands comma-separated words to split {@code args[]}.
     * @param args raw command line arguments to split and convert into key-value
     * arguments for each command.
     * @return list of command runner instances.
     */
    public List<CommandRunnerInstance> createCommandRunnerInstances(CommandRunner runner, String commands, String[] args) {
        return CommandRunnerImpl.getInstance().create(new ArrayList<>(), runner, commands, args);
    }

    /** {@inheritDoc} */
    @Override
    public Logger logger() { return log; }

    /** {@inheritDoc} */
    @Override
    public PropertiesImpl properties() { return properties; }

    /** {@inheritDoc} */
    @Override
    public SE1_Runtime startup(String[] args) {
        log.trace(String.format("%s: startup(String[] args) called", this.getClass().getSimpleName()));
        if(runtimeBuilder==null) {
            runtimeBuilder = new RuntimeBuilder();
            var runnableInstanceLauncher = runtimeBuilder.build(
                classes, runnableClasses, runnableInstances, properties
            );
            runnableInstanceLauncher.launch(args);
        }
        return this;
    }
}
