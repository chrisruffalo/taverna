package io.github.chrisruffalo.taverna.pki.dir;

import io.github.chrisruffalo.resultify.Result;
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

        final Result<List<Cert>> certsResult = dirLoader.load(config);
        Assertions.assertFalse(certsResult.isEmpty());

        final List<Cert> certs = certsResult.get();
        Assertions.assertFalse(certs.isEmpty());
    }
}