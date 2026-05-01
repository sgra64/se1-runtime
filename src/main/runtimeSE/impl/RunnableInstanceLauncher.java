package runtimeSE.impl;

import java.util.ArrayList;
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
                    // from full 'a.b.c.classname.args' or simple 'classname.args' property
                    // @Nullable - requires module import: 'org.jspecify'
                    String propertyArgs = properties.getProperty(runnable.getClass().getCanonicalName() + ".args", null);
                    propertyArgs = propertyArgs != null? propertyArgs : properties.getProperty(runnable.getClass().getSimpleName() + ".args", null);
                    if(propertyArgs != null) {
                        argsL.add(propertyArgs);
                    }
                }
                log.trace(String.format("%s: launching %s.run() for runnable: '%s' with args: [%s]", this.getClass().getSimpleName(),
                            runnable.getClass().getSimpleName(), runnable.getClass().getSimpleName(), argsL));
                // 
                runnable.run(RuntimeSE.getInstance(), argsL.toArray(new String[argsL.size()]));
            }
        }
        LoggerImpl.flushAppenders();
    }
}
