package runtime.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import runtime.Runner;
import runtime.Runner.Accessors;


class RunnableClassesSelector {

    private static RunnableClassesSelector instance = new RunnableClassesSelector();

    private RunnableClassesSelector() { }

    static RunnableClassesSelector getInstance() { return instance; }

    @SuppressWarnings("unchecked")
    void selectRunnableClasses(List<Class<Runner>> collect, List<Class<?>> classes, String[] names) {
        // 
        if(names != null && names.length > 0) {
            // found runnables in 'application.properties', match their order from classes
            for(var n : names) {
                classes.stream()
                    // filter classes that are assignable from interface {@link application.Runnable}
                    .filter(clazz -> Runner.class.isAssignableFrom(clazz))
                    .map(clazz -> (Class<Runner>)clazz)
                    .filter(clazz -> clazz.getCanonicalName().equals(n.trim())) // .contains(n.trim())
                    .collect(Collectors.toCollection(() -> collect));
                    // .findFirst()    // collect clazz with highest priority
                    // .map(clazz -> collect.add(clazz));
            }
        } else {
            // sort by @Runnable.Accessors(priority)
            classes.stream()
                // 
                // filter classes assignable from the {@link application.Runnable} interface
                // and sort by @Runnable.Accessors(priority)
                .filter(clazz -> Runner.class.isAssignableFrom(clazz))
                .map(clazz -> (Class<Runner>)clazz)
                // 
                .sorted( (c1, c2) -> {
                    int p1 = Optional.ofNullable(c1.getAnnotation(Accessors.class)).map(a -> a.priority()).orElse(-1);
                    int p2 = Optional.ofNullable(c2.getAnnotation(Accessors.class)).map(a -> a.priority()).orElse(-1);
                    // 
                    return p1==p2? c1.getSimpleName().compareTo(c2.getSimpleName()) : -Integer.compare(p1, p2);
                })
                .forEach(clazz -> collect.add(clazz));
                // .findFirst()    // collect clazz with highest priority
                // .map(clazz -> collect.add(clazz));
        }
    }
}
