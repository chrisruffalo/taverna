package io.github.chrisruffalo.taverna.meta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationPropertiesTest {

    @Test
    void version() {
        Assertions.assertNotNull(ApplicationProperties.version());
    }
}