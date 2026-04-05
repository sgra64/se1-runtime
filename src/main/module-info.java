
module runtime {

    /*
     * Make package {@code application} accessible to other modules at compile
     * and runtime (use <i>open</i> for compile-time access only).
     */
    exports runtime;

    /* Open package to JUnit test runner and Javadoc compiler. */
    opens runtime;

    requires org.junit.jupiter.api;
}
