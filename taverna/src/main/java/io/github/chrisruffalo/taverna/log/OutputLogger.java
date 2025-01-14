package io.github.chrisruffalo.taverna.log;

public interface OutputLogger {

    void infof(String template, Object... values);

    void errorf(String template, Object... values);

}
