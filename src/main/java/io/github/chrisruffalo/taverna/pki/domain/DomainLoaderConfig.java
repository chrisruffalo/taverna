package io.github.chrisruffalo.taverna.pki.domain;

import io.github.chrisruffalo.taverna.pki.LoaderConfig;

public record DomainLoaderConfig(String domainName, int port) implements LoaderConfig {

    public String domainName() {
        return domainName;
    }

    public int port() {
        return port;
    }

}
