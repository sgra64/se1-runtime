package runtimeSE.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import runtimeSE.CommandRunner;
import runtimeSE.Logger;
import runtimeSE.Runner;
import runtimeSE.RuntimeSE;
import runtimeSE.CommandRunner.CommandRunnerInstance;

/**
 * Class {@link RuntimeSE_Impl} provides the access point to non-public
 * implementation classes of the package {@code runtime.impl}.
 * @version <code style=color:green>{@value runtimeSE.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtimeSE.package_info#Author}</code>
 */
public final class RuntimeSE_Impl implements RuntimeSE {

    protected final static String LoggerName = "Runtime";

    private final static Logger log = Logger.getLogger(LoggerName);

    private static RuntimeSE_Impl runtimeSystem = null;

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

    private RuntimeSE_Impl() {
        log.info(String.format("Running on Java %s", System.getProperty("java.version")));
        log.info(String.format("%s, singleton instance created", this.getClass().getSimpleName()));
    }

    /**
     * Return the singleton instance of the runtime system. Create the instance
     * when it does not yet exist (lazy initialization).
     * @return the singleton instance of the runtime system.
     */
    public static RuntimeSE_Impl getInstance() {
        return Optional.ofNullable(runtimeSystem).orElseGet(() -> runtimeSystem = new RuntimeSE_Impl());
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
    public List<CommandRunnerInstance> createCommandRunnerInstances(CommandRunner runner, String commands, String args) {
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
    public RuntimeSE startup(String[] args) {
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

    /** {@inheritDoc} */
    @Override
    public String pickArgs(String key, String[] args) {
        String pargs = "";
        if(args == null || (args.length > 0 && args[0].isEmpty())) {
            logger().info("args[] null or empty, using args[] from 'numbers.args' property");
            pargs = Optional.ofNullable((String) properties().get("numbers.args")).orElseGet(() -> {
                logger().warn(String.format("%s: no property 'numbers.args' found, using empty args", this.getClass().getSimpleName()));
                return "";
            });
        } else {
            pargs = String.join(" ", args);
            logger().info(String.format("using args[] from command line: %s", pargs));
        }
        return pargs;
    }
}
