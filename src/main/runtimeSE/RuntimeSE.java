package runtimeSE;

import java.util.Properties;

/**
 * Interface {@link RuntimeSE} defines the public API of the {@link runtimeSE}
 * module.
 * @version <code style=color:green>{@value runtimeSE.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtimeSE.package_info#Author}</code>
 */
public interface RuntimeSE {

    /**
     * Return the singleton instance of the {@link RuntimeSE} implementation.
     * @return
     */
    static RuntimeSE getInstance() {
        return runtimeSE.impl.RuntimeSE_Impl.getInstance();
    }

    /**
     * Start up the runtime environment by scanning for classes that implement
     * the {@link Runner} interface, creating singleton <i>bean</i> objects and
     * invoking their {@code run(String[] args)} method depending on a
     * {@code priority} (see {@link Runner.Accessors}).
     * @param args command line arguments passed to the application.
     * @return chainable self-reference to the runtime instance.
     */
    RuntimeSE startup(String[] args);

    /**
     * Select {@code String[] args} from command line (preferred)  or from key
     * in 'application.properties', if no command line arguments are given.
     * Method flattens arguments to a single String.
     * @param args command line arguments.
     * @return arguments flattened to a single String.
     */
    String pickArgs(String key, String[] args);

    /**
     * Return {@link Properties} loaded from the {@code application.properties}
     * @return properties loaded from the {@code application.properties} file.
     */
    Properties properties();

    /**
     * Return the logger instance implementing the {@link Logger} interface.
     * @return logger instance that implements the {@link Logger} interface.
     */
    Logger logger();
}
