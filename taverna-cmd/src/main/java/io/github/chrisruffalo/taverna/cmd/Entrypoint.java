package io.github.chrisruffalo.taverna.cmd;

import com.beust.jcommander.JCommander;
import io.github.chrisruffalo.taverna.Engine;
import io.github.chrisruffalo.taverna.cmd.opt.CliOptions;
import io.github.chrisruffalo.taverna.log.SystemLogger;
import io.github.chrisruffalo.taverna.meta.ApplicationProperties;

/**
 * Entrypoint class
 */
public class Entrypoint {

    public static void main(String[] args) {
        final CliOptions cliOptions = new CliOptions();
        final JCommander jCommander = JCommander
                .newBuilder()
                .programName("taverna")
                .acceptUnknownOptions(true)
                .addObject(cliOptions)
                .build();

        boolean showHelp = false; // intellij wants me to remove this initializer but honestly that's not how i was taught so i won't. yes it does nothing.
        try {
            jCommander.parse(args);
            showHelp = cliOptions.isHelp();
        } catch (Exception e) {
            showHelp = true;
            System.err.println(e.getMessage());
        }

        if (cliOptions.isVersion()) {
            System.out.printf("%s - %s\n", jCommander.getProgramName(), ApplicationProperties.version());
            System.exit(0);
        }

        // if an error happens or the help is requested
        // show help and exit
        if (showHelp) {
            jCommander.usage();
            System.exit(0);
        }

        final Entrypoint entrypoint = new Entrypoint();
        final int code = entrypoint.run(cliOptions);
        if (code != 0) {
            System.exit(code);
        }
    }

    public int run(final CliOptions cliOptions) {
        final Engine engine = new Engine(new SystemLogger(), cliOptions);
        return engine.run();
    }

}
