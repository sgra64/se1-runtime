package runtime.impl;

import java.util.List;
import java.util.Optional;

import runtime.Runner;


class RuntimeBuilder {

    private final static runtime.Logger log = runtime.Logger.getLogger(RuntimeSystem.LoggerName);

    private final static String banner = 
        // https://patorjk.com/software/taag/#p=display&f=Small+Slant&t=Run+Time++1+.0
        // alt bears, https://patorjk.com/software/taag/#p=display&f=Bear&t=BBB
        "  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ \n" +
        "{     ________    ___     ___              _______              ___   ___     }\n" +
        "{    / __/ __/   <  /    / _ \\__ _____    /_  __(_)_ _  ___    <  /  / _ \\    }\n" +
        "{   _\\ \\/ _/ /\\/ / /    / , _/ // / _ \\    / / / /  ' \\/ -_)   / / _/ // /    }\n" +
        "{  /___/___/    /_/    /_/|_|\\_,_/_//_/   /_/ /_/_/_/_/\\__/   /_/ (_)___/     }\n" +
        "{                                                                             }\n" +
        "  ~ ~ SE1-Runtime System started  ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ \n\\\\";

    RuntimeBuilder() {
        log.info(String.format("%s, singleton instance created", this.getClass().getSimpleName()));
    }

    RunnableInstanceLauncher build(
        List<Class<?>> classes,
        List<Class<Runner>> runnableClasses,
        List<Runner> runnableInstances,
        PropertiesImpl properties
    ) {
        log.trace(String.format("%s: build() method called", this.getClass().getSimpleName()));
        ClassScanner classScanner = new ClassScanner();
        RunnableClassesSelector runnablesSelector = RunnableClassesSelector.getInstance();
        RunnableInstanceFactory runnableInstanceCreator = RunnableInstanceFactory.getInstance();
        // 
        // step 0: locate 'application.properties' file
        properties.loadProperties(PropertiesImpl.PROPERTIES_FILE, new String[] {
            ".",                    // local file is first priority
            "src/resources",        // 2nd priority, if present
            "target/resources",     // 3rd priority
            "CLASSPATH",            // if not found yet, look at CLASSPATH
            "JAR"                   // or inside jar if packaged as .jar
        });
        // Logger.configureStaticLoggers(properties);
        // 
        if(properties.match("application.banner", s -> s.toLowerCase().equals("true"))) {
            System.out.println(banner);
        }
        // 
        // step 1: retrieve all classes
        classScanner.scanClasses(classes);
        log.info(String.format("%s, scanned %d classes from the file system from the Java Class Loader", this.getClass().getSimpleName(), classes.size()));
        // classes.stream()
        //     .map(cls -> String.format("- %s", cls.getName()))
        //     .forEach(System.out::println);
        // 
        // step 2: select subset of classes that implement the {@link CommandLineRunner} interface
        String[] runnablesFromProperties = Optional.ofNullable(properties.get("application.run"))
            .map(p -> p.split("[\\s,]+"))
            .orElse(new String[] { });  // "application.Application", "application.Demo_1"
        // 
        runnablesSelector.selectRunnableClasses(runnableClasses, classes, runnablesFromProperties);
        log.info(String.format("%s, found %d runnable classes: %s", this.getClass().getSimpleName(), runnableClasses.size(), runnableClasses));
        // 
        // step 3: create instances (objects) of classes that implement the {link application.Runnable} interface
        // according to the {@link RunnableInstanceCreator.CreationPolicy}, either none (if no runnable classes
        // were found), one (CreationPolicy.FirstOnly) or all runnable classes
        // 
        var creationPolicy = properties.match("application.run.policy", s -> s.toLowerCase().equals("all"))?
                RunnableInstanceFactory.CreationPolicy.All :
                RunnableInstanceFactory.CreationPolicy.FirstOnly;
        // 
        runnableInstanceCreator.create(runnableInstances, runnableClasses, creationPolicy);
        // 
        log.info(String.format("%s, %d runnable instances created: %s", this.getClass().getSimpleName(),
                    runnableInstances.size(), runnableInstances.stream().map(inst -> inst.getClass().getSimpleName()).toList()));
        // 
        return new RunnableInstanceLauncher(properties, runnableInstances);
    }
}
