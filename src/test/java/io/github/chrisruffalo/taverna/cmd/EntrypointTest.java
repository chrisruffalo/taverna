package io.github.chrisruffalo.taverna.cmd;

import io.github.chrisruffalo.taverna.opt.OptGlobal;
import org.junit.jupiter.api.Test;

class EntrypointTest {

    @Test
    void google() {
        final OptGlobal global = new OptGlobal();
        global.getSource().add("src/test/resources/pem/google");
        global.getDomains().add("google.com");

        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.run(global);

    }

    @Test
    void cloudflareNoTrust() {
        final OptGlobal global = new OptGlobal();
        global.getDomains().add("cloudflare.com");

        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.run(global);

    }

    @Test
    void untrusted() {
        final OptGlobal global = new OptGlobal();
        global.getDomains().add("brain.lan");

        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.run(global);

    }
}