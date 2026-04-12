package runtime;

import runtime.impl.RuntimeSystem;

/**
 * Interface {@link Logger} defines the API of a simple logging utility
 * that emulates {@code log4j} logging.
 * <p>
 * Configuration of the logging utility is achieved by setting properties
 * in the {@code application.properties} file:
 * <ul>
 * <li>{@code logger.Runtime.threshold} defines the minimum logging level
 *  to passed log messages to appenders. Possible values are:
 *  <ul>
 *    <li>{@code FATAL} - log only fatal messages.</li>
 *    <li>{@code ERROR} - log only error and fatal messages.</li>
 *    <li>{@code WARN} - log only warn, error and fatal messages.</li>
 *    <li>{@code INFO} - log only info, warn, error and fatal messages.</li>
 *    <li>{@code DEBUG} - log only debug, info, warn, error and fatal messages.</li>
 *    <li>{@code TRACE, ALL} - log all messages.</li>
 *  </ul>
 * </li>
 * <li>{@code logger.Runtime.appenders} defines destinations of log messages.
 *  Possible values are {@code console} (for Console output) and names of log
 *  files.</li>
 * </ul>
 * @version <code style=color:green>{@value runtime.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtime.package_info#Author}</code>
 */
public interface Logger {

    /**
     * Return existing or new logger instance by name. Create logger instance
     * when name is not yet preset.
     * @param name name of the logger to return.
     * @return  logger instance of that name.
     */
    static Logger getLogger(String name) {
        return RuntimeSystem.getLogger(name);
    }

    /**
     * Log a fatal message. Fatal messages are the most severe messages that
     * indicate a critical failure in the application and typically lead to
     * application shut-down.
     * @param msg message to log.
     */
    void fatal(String msg);

    /**
     * Log an error message. Error messages indicate a failure in the
     * application that should be investigated and fixed, but typically do not
     * lead to application shut-down. Exceptions that are typically logged as
     * error messages.
     * @param msg message to log.
     */
    void error(String msg);

    /**
     * Log a warn message. Warn messages indicate a potential problem in the
     * application that does not cause a failure.
     * @param msg
     */
    void warn(String msg);

    /**
     * Log an info message. Info messages indicate a normal but significant
     * event in the application.
     * @param msg message to log.
     */
    void info(String msg);

    /**
     * Log a debug message. Debug messages indicate a detailed information about
     * the application that is useful for debugging and troubleshooting.
     * @param msg message to log.
     */
    void debug(String msg);

    /**
     * Log a trace message. Trace messages indicate a very detailed information
     * about the application that is useful for tracing the execution of the
     * application.
     * @param msg message to log.
     */
    void trace(String msg);
}
