package io.github.chrisruffalo.taverna.pki.store;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.pki.BaseLoader;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TrustStoreLoader extends BaseLoader<TrustStoreLoaderConfig> {

    @Override
    public List<Cert> load(TrustStoreLoaderConfig configuration) {
        final Path path = configuration.path();
        final String type = configuration.type();
        final String password = configuration.password();

        final Result<KeyStore> ksResult = loadKeystore(path, type, password);
        if (ksResult.isEmpty()) {
            return List.of();
        }

        final KeyStore keyStore = ksResult.get();
        final Result<PKIXParameters> pkixResults = Result.from(() -> new PKIXParameters(keyStore));
        if (pkixResults.isEmpty()) {
            return List.of();
        }
        final PKIXParameters parameters = pkixResults.get();

        final Set<TrustAnchor> anchors = parameters.getTrustAnchors();

        return anchors.stream()
                .map(TrustAnchor::getTrustedCert)
                .map(Cert::fromX509)
                .collect(Collectors.toList());
    }

    private Result<KeyStore> loadKeystore(final Path path, final String type, final String password) {
        return Result.from(() -> {
            final KeyStore keyStore = KeyStore.getInstance(type);
            try (final FileInputStream fis = new FileInputStream(path.toFile())) {
                keyStore.load(fis, password.toCharArray());
            }
            return keyStore;
        });
    }
}
