package io.github.chrisruffalo.taverna.pki.domain;

import io.github.chrisruffalo.taverna.opt.Options;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class DomainLoaderConfigTest {

    static Stream<Arguments> domainCheckValues() {
        return Stream.of(
          Arguments.of("google.com", "google.com", 443),
          Arguments.of("internal.ldap.host:689", "internal.ldap.host", 689),
          Arguments.of("https://cloudflare.com:1234", "cloudflare.com", 1234)
        );
    }

    @ParameterizedTest
    @MethodSource("domainCheckValues")
    void check(final String fullDomain, final String expectedDomain, final int expectedPort) {
        final DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig(fullDomain, Options.DEFAULT_SSL_PORT);
        Assertions.assertEquals(expectedDomain, domainLoaderConfig.getDomainName());
        Assertions.assertEquals(expectedPort, domainLoaderConfig.getDomainPort());
    }


}
