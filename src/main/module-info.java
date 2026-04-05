/**
 * Modules have been introduced in Java 9 (in 2017) to build software from modular
 * projects. Prior, applications could be built only from packages within a project.
 * <p>
 * See Jakob Jenkov's article:
 * <a href="https://jenkov.com/tutorials/java/modules.html"><i>Java Modules</i></a>.
 * </p><p>
 * {@code module-info.java} indicates a <i>modular</i> Java project. It includes
 * the module name: {@link se1_play}, external modules required by this module and
 * packages exported to other modules. Opening a package makes it accessible to
 * tools such as JUnit test runners. Javadoc requires packages open or exported.
 * </p><p>
 * Locations of <i>required</i> modules are provided by the {@code MODULEPATH}
 * environment variable.
 * </p>
 * @version <code style=color:green>{@value application.package_info#Version}</code>
 * @author <code style=color:blue>{@value application.package_info#Author}</code>
 */
module se1_play {

    /*
     * Make package {@code application} accessible to other modules at compile
     * and runtime (use <i>open</i> for compile-time access only).
     */
    exports application;

    /* Open package to JUnit test runner and Javadoc compiler. */
    opens application;

    /*
     * External module required by this module (JUnit-5 module for JUnit testing).
     */
    // requires org.junit.jupiter.api;
}
