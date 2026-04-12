/**
 * Module declaration of the {@link runtime} module that provides a simple
 * runtime environment for Java applications with support for:
 * <ul>
 * <li>{@code application.properties} file.</li>
 * <li>a simple {@code logging} utility.</li>
 * <li><i>Inversion-of-Control</i> by scanning for class that implement the
 *  {@link runtime.Runner} interface, creating singleton <i>bean</i> objects
 *  and invoking their {@code run(String[] args)} method depending on a
 *  {@code priority} (see {@link runtime.Runner.Accessors}).</li>
 * <li>advanced {@link runtime.CommandRunner} interface with command splitting
 *  and {@code String[] args} type conversion.</li>
 * </ul>
 */
module runtime {

    /*
     * Make package accessible to other modules at compile and runtime.
     */
    exports runtime;

    /* Open package to JUnit test runner and Javadoc compiler. */
    opens runtime;

    requires org.junit.jupiter.api;
}
