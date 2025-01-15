package io.github.chrisruffalo.taverna.pki.store;

import io.github.chrisruffalo.taverna.pki.LoaderConfig;

import java.nio.file.Path;
import java.security.KeyStore;

public record TrustStoreLoaderConfig(Path path, String type, String password) implements LoaderConfig {

    public Path path() {
        return path;
    }

    public String type() {
        if (type != null && !type.isEmpty()) {
            return type;
        }
        return KeyStore.getDefaultType();
    }

    public String password() {
        return password;
    }

}
