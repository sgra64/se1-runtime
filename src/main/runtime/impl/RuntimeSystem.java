package runtime.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import runtime.CommandRunner;
import runtime.CommandRunner.CommandRunnerInstance;
import runtime.Logger;
import runtime.SE1_Runtime;
import runtime.Runner;


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

    public static RuntimeSystem getInstance() {
        return Optional.ofNullable(runtimeSystem).orElseGet(() -> runtimeSystem = new RuntimeSystem());
    }

    /**
     * Return named logger instance. Create when name is not yet preset.
     * @param name logger name
     * @return named logger
     */
    public static Logger getLogger(String name) { return LoggerImpl.getLogger(name); }


    public List<CommandRunnerInstance> createCommandRunnerInstances(CommandRunner runnable, String commands, String[] args) {
        return CommandRunnerImpl.getInstance().create(new ArrayList<>(), runnable, commands, args);
    }

    @Override
    public Logger logger() { return log; }

    @Override
    public PropertiesImpl properties() { return properties; }

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
