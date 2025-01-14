package io.github.chrisruffalo.taverna.pki.file;

import io.github.chrisruffalo.taverna.model.Cert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

class FileLoaderTest {

    @Test
    void loadGoogleRoot1() {
        final FileLoaderConfig config = new FileLoaderConfig(
            Paths.get("src/test/resources/pem/google/root/r1.pem")
        );

        final FileLoader loader = new FileLoader();

        final List<Cert> certs = loader.load(config);

        Assertions.assertFalse(certs.isEmpty());
    }

    @Test
    void loadGoogleAllRoots() {
        final FileLoaderConfig config = new FileLoaderConfig(
            Paths.get("src/test/resources/pem/combined/google-root.pem")
        );

        final FileLoader loader = new FileLoader();

        final List<Cert> certs = loader.load(config);

        Assertions.assertFalse(certs.isEmpty());
        Assertions.assertEquals(5, certs.size());
    }

}