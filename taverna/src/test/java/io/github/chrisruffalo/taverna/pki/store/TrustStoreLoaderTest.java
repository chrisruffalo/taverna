package io.github.chrisruffalo.taverna.pki.store;


import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

class TrustStoreLoaderTest {

    @Test
    void load() {
        final TrustStoreLoaderConfig config = new TrustStoreLoaderConfig(
            Paths.get("src/test/resources/stores/trust.jks"),
            "jks",
            "changeit"
        );
        final TrustStoreLoader loader = new TrustStoreLoader();

        final Result<List<Cert>> certsResult =  loader.load(config);
        Assertions.assertFalse(certsResult.isEmpty());

        final List<Cert> certs = certsResult.get();
        Assertions.assertNotNull(certs);
        Assertions.assertEquals(2, certs.size());
    }

    @Test
    void loadPKCS12() {
        final TrustStoreLoaderConfig config = new TrustStoreLoaderConfig(
            Paths.get("src/test/resources/stores/trust.p12"),
            "pkcs12",
            "changeit"
        );
        final TrustStoreLoader loader = new TrustStoreLoader();

        final Result<List<Cert>> certsResult =  loader.load(config);
        Assertions.assertFalse(certsResult.isEmpty());

        final List<Cert> certs = certsResult.get();
        Assertions.assertNotNull(certs);
        Assertions.assertEquals(5, certs.size());
    }
}