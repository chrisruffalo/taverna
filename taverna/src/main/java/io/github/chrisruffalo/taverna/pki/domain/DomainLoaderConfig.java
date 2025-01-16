package io.github.chrisruffalo.taverna.pki.domain;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.pki.LoaderConfig;

import java.net.URI;

public class DomainLoaderConfig implements LoaderConfig {


    private final String domainName;

    private final int domainPort;

    public DomainLoaderConfig(String fullDomain, int defaultPort) {
        if (fullDomain == null || fullDomain.isEmpty()) {
            throw new IllegalArgumentException("domain name cannot be empty");
        }
        // first try uri-based parsing
        Result<URI> uri = Result.from(() -> URI.create(fullDomain));
        if (uri.isPresent() && uri.get().getHost() != null) {
            domainName = uri.get().getHost();
            domainPort = uri.get().getPort();
        } else if (fullDomain.contains(":")) {
            final String[] parts = fullDomain.split(":");
            domainName = parts[0];
            if (parts.length > 1) {
                domainPort = Result.of(parts[1]).map(Integer::parseInt).getOrFailsafe(defaultPort);
            } else {
                domainPort = defaultPort;
            }
        } else {
            domainName = fullDomain;
            domainPort = defaultPort;
        }
    }

    public String getDomainName() {
        return domainName;
    }

    public int getDomainPort() {
        return domainPort;
    }
}
