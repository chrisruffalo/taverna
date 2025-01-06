package io.github.chrisruffalo.taverna.pki.domain;

import io.github.chrisruffalo.taverna.model.Cert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DomainLoaderTest {

    @Test
    void loadGoogle() {
        DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig("google.com", 443);
        DomainLoader loader = new DomainLoader();
        final List<Cert> certs = loader.load(domainLoaderConfig);

        Assertions.assertFalse(certs.isEmpty());
    }

    @Test
    void loadUntrusted() {
        DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig("brain.lan", 443);
        DomainLoader loader = new DomainLoader();
        final List<Cert> certs = loader.load(domainLoaderConfig);

        Assertions.assertFalse(certs.isEmpty());
    }

}