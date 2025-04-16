package io.github.chrisruffalo.taverna.bc;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.opt.DefaultOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.Provider;

class BouncyCastleClasspathLoaderTest {

    @Test
    void bcfips() {
        final Result<Provider> bcResult = BouncyCastleLoader.get(new DefaultOptions()).getBouncyCastleFipsProvider(new DefaultOptions());
        Assertions.assertFalse(bcResult.isError());
        final Provider bcProvider = bcResult.get();
        Assertions.assertNotNull(bcProvider);
        Assertions.assertInstanceOf(Provider.class, bcProvider);
    }

    @Test
    void bc() {
        final Result<Provider> bcResult = BouncyCastleLoader.get(new DefaultOptions()).getBouncyCastleProvider(new DefaultOptions());
        Assertions.assertFalse(bcResult.isError());
        final Provider bcProvider = bcResult.get();
        Assertions.assertNotNull(bcProvider);
        Assertions.assertInstanceOf(Provider.class, bcProvider);
    }

}