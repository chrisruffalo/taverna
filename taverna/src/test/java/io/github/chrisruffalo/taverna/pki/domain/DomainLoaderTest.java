package io.github.chrisruffalo.taverna.pki.domain;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DomainLoaderTest {

    @Test
    void loadGoogle() {
        DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig("google.com", 443);
        DomainLoader loader = new DomainLoader();

        final Result<List<Cert>> certs = loader.load(domainLoaderConfig);
        Assertions.assertFalse(certs.isEmpty());

        final List<Cert> certList = certs.get();
        Assertions.assertFalse(certList.isEmpty());
    }

    @Test
    void badDomain() {
        DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig("wlkjalksdjflkasdfljasdflkjadsfljasdlfkjasdflkjasdflfasd.io", 1443);
        DomainLoader loader = new DomainLoader();

        final Result<List<Cert>> certs = loader.load(domainLoaderConfig);
        Assertions.assertTrue(certs.isEmpty());
        Assertions.assertTrue(certs.isError());
    }

    @Test
    void badPort() {
        DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig("google.com", 1443);
        DomainLoader loader = new DomainLoader();

        final Result<List<Cert>> certs = loader.load(domainLoaderConfig);
        Assertions.assertTrue(certs.isEmpty());
        Assertions.assertTrue(certs.isError());
    }

}