package runtime;

import runtime.impl.RuntimeSystem;


public interface Logger {

    /**
     * Return named logger instance. Create when name is not yet preset.
     * @param name logger name
     * @return named logger
     */
    static Logger getLogger(String name) {
        return RuntimeSystem.getLogger(name);
    }

    void fatal(String msg);

    void error(String msg);

    void warn(String msg);

    void info(String msg);

    void debug(String msg);

    void trace(String msg);
}
