package io.github.chrisruffalo.taverna.cmd;

import io.github.chrisruffalo.taverna.cmd.opt.CliOptions;
import org.junit.jupiter.api.Test;

class EntrypointTest {

    @Test
    void google() {
        final CliOptions global = new CliOptions();
        global.getSource().add("src/test/resources/pem/google");
        global.getDomains().add("google.com");

        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.run(global);

    }

    @Test
    void cloudflareNoTrust() {
        final CliOptions global = new CliOptions();
        global.getDomains().add("cloudflare.com");

        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.run(global);

    }

    @Test
    void untrusted() {
        final CliOptions global = new CliOptions();
        global.getDomains().add("brain.lan");

        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.run(global);

    }
}