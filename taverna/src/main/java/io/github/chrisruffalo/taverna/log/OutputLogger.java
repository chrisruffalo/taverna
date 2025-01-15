package io.github.chrisruffalo.taverna.log;

/**
 * Quick and easy logger wrapper to allow the engine
 * to adapt to different logging environments, where
 * necessary.
 */
public interface OutputLogger {

    /**
     * Output a message to the log at the INFO level
     *
     * @param template to use
     * @param values values to populate template with
     */
    void infof(String template, Object... values);

    /**
     * Output a message to the log at the ERROR level
     *
     * @param template to use
     * @param values values to populate template with
     */
    void errorf(String template, Object... values);

}
