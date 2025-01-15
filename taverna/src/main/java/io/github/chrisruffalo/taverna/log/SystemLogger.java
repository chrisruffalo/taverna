package io.github.chrisruffalo.taverna.log;

/**
 * Easy implementation of the output logger built on
 * System.out/System.err.
 */
public class SystemLogger implements OutputLogger {

    @Override
    public void infof(String template, Object... values) {
        System.out.printf(template + "\n", values);
    }

    @Override
    public void errorf(String template, Object... values) {
        System.err.printf(template + "\n", values);
    }
}
