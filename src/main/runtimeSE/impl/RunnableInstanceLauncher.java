package runtimeSE.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import runtimeSE.Runner;
import runtimeSE.RuntimeSE;


class RunnableInstanceLauncher {

    private final static runtimeSE.Logger log = runtimeSE.Logger.getLogger(RuntimeSE_Impl.LoggerName);
    private final Properties properties;
    private final List<Runner> runnableInstances;


    RunnableInstanceLauncher(Properties properties, List<Runner> runnableInstances) {
        this.properties = properties;
        this.runnableInstances = runnableInstances;
    }

    void launch(String[] args) {
        // 
        String p1 = Optional.ofNullable(System.getProperty("user.dir")).map(p -> p.replaceAll("\\\\", "/")).orElse("no-match-xxx");
        p1 = p1.substring(0, Math.min(15, p1.length())).toLowerCase();  // match path start (15 chars)
        // 
        // step 4: collect args[] passed from command line in 'argsL'
        List<String> argsL = new ArrayList<>();
        for(int i=0; args != null && i < args.length; i++) {
            String arg = args[i];
            // 
            // (***) VSCode CodeRunner injects open file as first arg with full path, skip this arg at position 0
            String p2 = arg.replaceAll("\\\\", "/").toLowerCase();
            if(i==0 && (p2.startsWith(p1) || p2.contains("code-runner-#1-Code"))) {
                log.warn(String.format("%s: removed arg[0] (likely injected by VSCode Runner): [%s]", this.getClass().getSimpleName(), arg));
            } else {
                argsL.add(arg);
            }
        }
        // step 5: invoke the {@code run(String[] args)} method on created runnable instances
        for(var runnable : runnableInstances) {
            // 
            // combine commandLineArgs and propertyArgs and invoke runnable's run() method
            if(Runner.class.isAssignableFrom(runnable.getClass())) {
                // 
                if(argsL.size()==0) {
                    // if no args have been passed from the command line, attempt to collect args[]
                    // from runnable class name + '.args' in 'application.properties', e.g. 'numbers.args'
                    // @Nullable - requires module import: 'org.jspecify'
                    String propertyKey = null;
                    String propertyArgs = null;
                    HashSet<String> keyAttempts = new HashSet<>();
                    for(String k : List.of(
                        runnable.getClass().getCanonicalName(),
                        runnable.getClass().getPackageName(),   // full package name
                        runnable.getClass().getPackageName().replaceAll(".*\\.", "")    // last name in package
                    )) {
                        propertyKey = k + ".args";
                        propertyArgs = properties.getProperty(propertyKey, null);
                        if(propertyArgs==null) {
                            keyAttempts.add(propertyKey);
                        } else {
                            break;
                        }
                    }
                    if(propertyArgs != null) {
                        // split single-string propertyArgs into args[] by white spaces,
                        // except in single- or double quotes and collect in argsL list
                        argsSplitter(argsL, propertyArgs);
                        log.trace(String.format("%s: picked args[] from property: '%s'", this.getClass().getSimpleName(), propertyKey));
                    } else {
                        log.warn(String.format("%s: no property in 'application.properties' to pick-up args[] using keys: '%s'",
                            this.getClass().getSimpleName(), keyAttempts));
                    }
                    keyAttempts.clear();
                }
                log.trace(String.format("%s: launching %s.run() for runnable: '%s' with args: %s", this.getClass().getSimpleName(),
                            runnable.getClass().getSimpleName(), runnable.getClass().getSimpleName(), argsL));
                // 
                runnable.run(RuntimeSE.getInstance(), argsL.toArray(new String[argsL.size()]));
            }
        }
        LoggerImpl.flushAppenders();
    }

    /**
     * Split single-string arg, e.g. from propertyArgs, into args[] by
     * white spaces, but not in single- or double quotes.
     */
    private void argsSplitter(List<String> result, String singleStringArgs) {
        StringBuilder sb = new StringBuilder();
        boolean inDoubleQuotes = false, inSingleQuotes = false;
        // 
        for (char c : singleStringArgs.toCharArray()) {
            if (c == '\"' && ! inSingleQuotes) inDoubleQuotes = ! inDoubleQuotes;
            else if (c == '\'' && ! inDoubleQuotes) inSingleQuotes = ! inSingleQuotes;
            // 
            if (Character.isWhitespace(c) && ! inDoubleQuotes && ! inSingleQuotes) {
                if (sb.length() > 0) {
                    result.add(trimQuotes(sb.toString()));
                    sb.setLength(0);
                }
            } else {
                sb.append(c);
            }
        }
        result.add(trimQuotes(sb.toString()));
    }

    /**
     * Trim leading and trailing single or double quotes.
     */
    private String trimQuotes(String str) {
        if (str == null || str.length() < 2) return str;
        // 
        if ((str.startsWith("\"") && str.endsWith("\"")) || 
            (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}
