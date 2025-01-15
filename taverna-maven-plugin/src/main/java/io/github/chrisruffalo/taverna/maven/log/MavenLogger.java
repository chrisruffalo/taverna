package io.github.chrisruffalo.taverna.maven.log;

import io.github.chrisruffalo.taverna.log.OutputLogger;
import org.apache.maven.plugin.logging.Log;

/**
 * Wrapper class to adapt output logs from taverna engine
 * into maven logs
 */
public class MavenLogger implements OutputLogger {

    private final Log log;

    public MavenLogger(Log log) {
        this.log = log;
    }

    @Override
    public void infof(String template, Object... values) {
        log.info(String.format(template, values));
    }

    @Override
    public void errorf(String template, Object... values) {
        log.error(String.format(template, values));
    }
}
