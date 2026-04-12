package application;

import runtime.SE1_Runtime;
import runtime.Runner;
import runtime.Runner.Accessors;

/**
 * Class {@link Application} is a simple application that implements the
 * interface {@link Runner} to demonstrate the runtime system. Method
 * {@code run(String[] args)} is the entry point of the application.
 * @version <code style=color:green>{@value runtime.package_info#Version}</code>
 * @author <code style=color:blue>{@value runtime.package_info#Author}</code>
 */
@Accessors(priority=0)
public class Application implements Runner {

    /**
     * The main method is the entry point of the Java application.
     * @param args arguments passed from the command line.
     */
    public static void main(String[] args) {
        SE1_Runtime.getInstance().startup(args);
    }

    /** {@inheritDoc} */
    @Override
    public void run(String[] args) {
        System.out.println(String.format("Hello, %s!", this.getClass().getSimpleName()));
    }
}
