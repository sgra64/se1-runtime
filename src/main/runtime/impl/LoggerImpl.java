package runtime.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class LoggerImpl implements runtime.Logger {
    private static final String[] levels = new String[] { "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF" };
    private static final DateTimeFormatter fmtDateTime = DateTimeFormatter.ofPattern("yyyy-MMdd HH:mm:ss:SSS");
    private static final Map<String, LoggerImpl> loggers = new HashMap<>();
    private static final Map<String, Appender> appenders = new HashMap<>();
    static {
        loggers.put(RuntimeSystem.LoggerName, new LoggerImpl(RuntimeSystem.LoggerName));
        appenders.put("Console", new ConsoleAppender("Console"));
    }

    private int threshold = -1;
    private boolean configured = false;
    private final String name;
    private final List<Appender> loggerAppenders = new ArrayList<>();
    private StringBuilder preLog = new StringBuilder();     // store log-lines before logger has been configured

    private LoggerImpl(String name) {
        this.name = name;
        loggers.put(name, this);
    }

    /**
     * Return named logger  instance. Create when name is not yet preset.
     * @param name logger name
     * @return named logger
     */
    public static runtime.Logger getLogger(String name) {
        return Optional.ofNullable(loggers.get(name==null || name.length()==0? RuntimeSystem.LoggerName : name))
            .orElseGet(() -> { LoggerImpl log; loggers.put(name, log = new LoggerImpl(name)); return log; });
    }

    @Override
    public void fatal(String msg) { output(6, msg); }
    
    @Override
    public void error(String msg) { output(5, msg); }
    
    @Override
    public void warn(String msg)  { output(4, msg); }
    
    @Override
    public void info(String msg)  { output(3, msg); }
    
    @Override
    public void debug(String msg) { output(2, msg); }
    
    @Override
    public void trace(String msg) { output(1, msg); }


    private void output(int level, String msg) {
        if(level >= threshold) {
            // format log line
            String d = level==0? "" : LocalDateTime.now().format(fmtDateTime);
            String logLine = level==0? preLog.toString() : String.format("[%s] [%s] [%-5s] - %s", d, name, levels[level], msg);
            // 
            if( ! configured) {
                preLog.append(logLine).append("\n");
            } else {
                // output log line to appenders
                loggerAppenders.forEach(appender -> appender.append(logLine));
            }
        }
    }

    /**
     * Configure static loggers after ApplicationProperties have been initialized.
     * @param properties to configure loggers
     */
    public static void configureStaticLoggers(Properties properties) {
        loggers.values().forEach(logger -> {
            logger.configure(properties);
        });
    }

    public static void flushAppenders() {
        appenders.values().stream()
            .filter(app -> app instanceof FileAppender)
            .forEach(app -> app.flush());
    }

    private void configure(Properties properties) {
        if( ! configured) {
            // configure output threshold from logger property (if present)
            this.threshold = Optional.ofNullable(
                    properties.get(String.format("logger.%s.threshold", name))
                )
                .map(prop -> IntStream.range(0, levels.length)
                    .filter(i -> levels[i].equals(((String)prop).toUpperCase()))
                    .findFirst().orElse(threshold)
                ).orElse(threshold);    // threshold unchanged
            // 
            // configure appenders from logger property (if present)
            if(loggerAppenders.size()==0) {
                Optional.ofNullable(
                    properties.get(String.format("logger.%s.appenders", name))
                ).ifPresent(prop -> {
                    // parse list of appenders from property
                    Stream.of(((String)prop).split("[\\s,]+"))
                        .map(p -> {
                            // Appender app = appenders.get(p);
                            Appender app = appenders.entrySet().stream()
                                .filter(e -> e.getKey().toLowerCase().equals(p.toLowerCase()))
                                .map(e -> e.getValue())
                                .findFirst().orElse(null);
                            // 
                            if(app==null && p.endsWith(".log") &&
                                    ! this.loggerAppenders.stream().anyMatch(app2 -> app2.name().equals(p)))
                            {
                                app = new FileAppender(p);
                                appenders.put(p, app);
                            }
                            return app;
                        })
                        .filter(app -> app != null)
                        .forEach(app -> this.loggerAppenders.add(app));
                });
            }
            configured = true;
            // flush cached log-lines before logger has been configured
            output(0, preLog.deleteCharAt(preLog.length()-1).toString());
            preLog.setLength(0);
            preLog = null;
        }
    }

    private static abstract class Appender {
        private final String name;
        Appender(String name) { this.name = name; }
        String name() { return name; }
        abstract void append(String log);
        void flush() { }
    }

    private static class ConsoleAppender extends Appender {
        ConsoleAppender(String name) { super(name); }
        @Override void append(String log) { System.out.println(log); }
    }

    private static class FileAppender extends Appender {
        private static final int flushSize = 10 & 1024;   // 10kB
        private final StringBuilder buffer = new StringBuilder();
        private String filename = null;
        private boolean disabled = false;

        FileAppender(String filename) {
            super(filename);
        }

        @Override
        void append(String log) {
            buffer.append(log).append("\n");
            // 
            if(buffer.length() > flushSize) {
                flush();
            }
        }

        void flush() {
            if( ! disabled) {
                if(filename==null) {
                    try {
                        Path path = Paths.get(name());
                        if(path.getParent() != null) {
                            Files.createDirectories(path.getParent());
                        }
                        path = Files.exists(path)? path.toAbsolutePath() : Files.createFile(path);
                        this.filename = path.toString();
                        this.buffer.insert(0, String.format("+ new log opened%s\n", " -".repeat(18)));
                        // System.out.println(String.format("--> path: [%s], file: [%s]", path.toString(), filename));
                    } catch (IOException|InvalidPathException e) {
                        // e.printStackTrace();
                        System.err.println("IOException: " + e.getMessage());
                    }
                }
                if(filename != null) {
                    try(FileWriter fw = new FileWriter(filename, true);
                        BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.append(buffer);
                        buffer.setLength(0);
                    } catch(IOException e) {
                        // e.printStackTrace(); // System.err.println("IOException: " + e.getMessage());
                        System.err.println("IOException: " + e.getMessage());
                    }
                } else {
                    System.err.println(String.format("logger appender '%s' disabled, no valid filename: null", name()));
                    disabled = true;
                }
            }
        }
    }
}
