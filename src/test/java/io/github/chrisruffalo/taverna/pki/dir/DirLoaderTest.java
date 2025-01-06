package io.github.chrisruffalo.taverna.pki.dir;

import io.github.chrisruffalo.taverna.model.Cert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

class DirLoaderTest {

    @Test
    void load() {
        final DirLoaderConfig config = new DirLoaderConfig(Paths.get("src/test/resources/pem/google"));
        final DirLoader dirLoader = new DirLoader();

        final List<Cert> certs = dirLoader.load(config);

        Assertions.assertFalse(certs.isEmpty());
    }
}