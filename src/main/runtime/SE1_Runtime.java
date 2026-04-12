package runtime;

import java.util.Properties;

/**
 * Interface {@link SE1_Runtime} defines the public API of the {@link runtime}
 * module.
 * @version <code style=color:green>{@value runtime.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtime.package_info#Author}</code>
 */
public interface SE1_Runtime {

    /**
     * Return the singleton instance of the {@link SE1_Runtime} implementation.
     * @return
     */
    static SE1_Runtime getInstance() {
        return runtime.impl.RuntimeSystem.getInstance();
    }

    /**
     * Start up the runtime environment by scanning for classes that implement
     * the {@link Runner} interface, creating singleton <i>bean</i> objects and
     * invoking their {@code run(String[] args)} method depending on a
     * {@code priority} (see {@link Runner.Accessors}).
     * @param args command line arguments passed to the application.
     * @return chainable self-reference to the runtime instance.
     */
    SE1_Runtime startup(String[] args);

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
