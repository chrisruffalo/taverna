package io.github.chrisruffalo.taverna.bc;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.opt.Options;

import java.security.Provider;

public interface BouncyCastleLoader {

    String BCFIPS_NAME = "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider";
    String BCFIPS_JAR = "bc-fips-2.1.0.jar";

    String BC_NAME = "org.bouncycastle.jce.provider.BouncyCastleProvider";
    String BC_JAR = "bcprov-jdk18on-1.80.jar";

    Result<Provider> getBouncyCastleFipsProvider(Options options);

    Result<Provider> getBouncyCastleProvider(Options options);

    static BouncyCastleLoader get(Options options) {
        // do something here to load the SPI but fallback to the classpath loader
        return new BouncyCastleClasspathLoader();
    }

}
