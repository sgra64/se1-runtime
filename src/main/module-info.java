/**
 * Module declaration of the {@link runtimeSE} module that provides a simple
 * runtime environment for Java applications with support for:
 * <ul>
 * <li>{@code application.properties} file.</li>
 * <li>a simple {@code logging} utility.</li>
 * <li><i>Inversion-of-Control</i> by scanning for class that implement the
 *  {@link runtimeSE.Runner} interface, creating singleton <i>bean</i> objects
 *  and invoking their {@code run(String[] args)} method depending on a
 *  {@code priority} (see {@link runtimeSE.Runner.Accessors}).</li>
 * <li>advanced {@link runtimeSE.CommandRunner} interface with command splitting
 *  and {@code String[] args} type conversion.</li>
 * </ul>
 */
module runtimeSE {

    /*
     * Make package accessible to other modules at compile and runtime.
     */
    exports runtimeSE;

    /* Open package to JUnit test runner and Javadoc compiler. */
    opens runtimeSE;

    requires org.junit.jupiter.api;
}
