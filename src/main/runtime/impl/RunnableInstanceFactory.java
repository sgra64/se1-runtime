package runtime.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import runtime.Runner;


class RunnableInstanceFactory {

    private static final runtime.Logger log = runtime.Logger.getLogger(RuntimeSystem.LoggerName);

    private static RunnableInstanceFactory instance = new RunnableInstanceFactory();

    enum CreationPolicy { FirstOnly, All };

    private RunnableInstanceFactory() { }

    static RunnableInstanceFactory getInstance() { return instance; }

    void create(List<Runner> runnableInstances, List<Class<Runner>> runnableClasses, CreationPolicy creationPolicy) {
        runnableClasses = creationPolicy==CreationPolicy.All? runnableClasses : (runnableClasses.size() > 0? List.of(runnableClasses.get(0)) : List.of());
        runnableClasses.stream()
            .map(clazz -> createRunnable(clazz)).flatMap(Optional::stream)
            .collect(Collectors.toCollection(() -> runnableInstances));
    }

    private Optional<Runner> createRunnable(Class<Runner> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor();   // default constructor
            constructor.setAccessible(true);    // allow private construtors
            var runnable = (Runner)constructor.newInstance();
            log.info("Runnable instance created: " + runnable.getClass().getSimpleName());
            return Optional.of(runnable);
        // 
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(String.format("%s while scannung classes, %s", e.getClass().getSimpleName(), e.getMessage()));
        }
        return Optional.empty();
    }
}
