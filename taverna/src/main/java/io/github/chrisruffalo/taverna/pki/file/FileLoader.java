package io.github.chrisruffalo.taverna.pki.file;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.pki.BaseLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

public class FileLoader extends BaseLoader<FileLoaderConfig> {

    @Override
    public Result<List<Cert>> load(FileLoaderConfig configuration) {
        final Path path = configuration.path();
        if (path == null || !Files.isRegularFile(path)) {
            return Result.empty();
        }

        final Result<CertificateFactory> factoryResult = Result.from(() -> CertificateFactory.getInstance("X.509"));
        if (factoryResult.isError()) {
            return Result.of(null, factoryResult.error());
        } else if (factoryResult.isEmpty()) {
            return Result.empty();
        }

        final CertificateFactory cf = factoryResult.get();

        return loadCertificates(cf, configuration.path());
    }

    private Result<List<Cert>> loadCertificates(CertificateFactory cf, final Path path) {
        return Result.from(() -> {
            try (InputStream is = Files.newInputStream(path)) {
                return cf.generateCertificates(is)
                        .stream()
                        .filter(certificate -> certificate instanceof X509Certificate)
                        .map(X509Certificate.class::cast)
                        .map(Cert::fromX509).toList();
            }
        });
    }
}
