package io.github.chrisruffalo.taverna.pki.store;


import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.List;

/**
 * This is a repeated test to ensure that the classloading
 * works when using taverna dependency from another source
 * (like in another project or in a shaded jar)
 */
class TrustStoreLoaderTest {

    @Test
    void load() {
        final TrustStoreLoaderConfig config = new TrustStoreLoaderConfig(
            Paths.get("../taverna/src/test/resources/stores/trust.jks"),
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
            Paths.get("../taverna/src/test/resources/stores/trust.p12"),
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

    @Test
    void loadBKS() {
        final TrustStoreLoaderConfig config = new TrustStoreLoaderConfig(
                Paths.get("../taverna/src/test/resources/stores/trust.bks"),
                "bks",
                "changeit"
        );
        final TrustStoreLoader loader = new TrustStoreLoader();
        final KeyStore ks = loader.loadKeystore(config.path(), config.type(), config.password()).panicOrGet();
        Assertions.assertNotNull(ks);

        final Result<List<Cert>> certsResult =  loader.load(config);
        Assertions.assertFalse(certsResult.isEmpty());

        final List<Cert> certs = certsResult.get();
        Assertions.assertNotNull(certs);
        Assertions.assertEquals(2, certs.size());
    }

    @Test
    void loadBCFKS() {
        final TrustStoreLoaderConfig config = new TrustStoreLoaderConfig(
                Paths.get("../taverna/src/test/resources/stores/trust.bcfks"),
                "bcfks",
                "changeit"
        );
        final TrustStoreLoader loader = new TrustStoreLoader();
        final KeyStore ks = loader.loadKeystore(config.path(), config.type(), config.password()).panicOrGet();
        Assertions.assertNotNull(ks);

        final Result<List<Cert>> certsResult =  loader.load(config);
        Assertions.assertFalse(certsResult.isEmpty());

        final List<Cert> certs = certsResult.get();
        Assertions.assertNotNull(certs);
        Assertions.assertEquals(2, certs.size());
    }
}