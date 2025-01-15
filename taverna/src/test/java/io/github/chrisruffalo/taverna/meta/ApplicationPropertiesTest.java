package io.github.chrisruffalo.taverna.meta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApplicationPropertiesTest {

    @Test
    void version() {
        Assertions.assertNotNull(ApplicationProperties.version());
    }
}