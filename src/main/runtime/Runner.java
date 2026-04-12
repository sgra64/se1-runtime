package runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface {@link Runner} defines the contract for <i>"runnable"</i> classes
 * that are detected during class scanning and selected by the
 * {@code application.run.policy} property.
 * Property value {@code all} means that all detected classes are instantiated
 * as beans and their {@code run(String[] args)} method is launched in order
 * of their {@link Runner.Accessors} {@code priority}.
 * Property value {@code first-only} means only one of the detected classes
 * with the highest {@link Runner.Accessors} {@code priority} is instantiated
 * and launched.
 * @version <code style=color:green>{@value runtime.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtime.package_info#Author}</code>
 */
public interface Runner {

    /**
     * Annotation {@link Runner.Accessors} is used to define the order by
     * {@code priority}) of a class that implements the {@link Runner} interface
     * for bean instantiation and execution by the {@link runtime} system.
     * The higher the {@code priority} value, the earlier the class is instantiated
     * and executed. Default {@code priority} value is -1.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Accessors {
        public int priority() default -1;
    }

    /**
     * Method {@code run} is called by the {@link runtime} system after
     * instantiation of a class that implements this interface.
     * @param args arguments passed from the command line.
     */
    void run(String[] args);
}
