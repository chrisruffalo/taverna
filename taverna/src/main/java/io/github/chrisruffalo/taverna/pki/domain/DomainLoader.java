package io.github.chrisruffalo.taverna.pki.domain;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.pki.BaseLoader;

import javax.net.ssl.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class DomainLoader extends BaseLoader<DomainLoaderConfig> {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public Result<List<Cert>> load(DomainLoaderConfig configuration) {
        return loadCert(configuration.getDomainName(), configuration.getDomainPort());
    }

    private Result<List<Cert>> loadCert(String domain, int port) {
        return Result.from(() -> {
            // because we want to know what is going on at the remote we
            // never want to try and determine trust at this phase
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[] { tm }, null);

            final Callable<Result<List<Cert>>> certResultCallable = getResultCallable(domain, port, sslContext);

            final Future<Result<List<Cert>>> future = EXECUTOR.submit(certResultCallable);

            // todo: make this timeout configurable
            final Result<List<Cert>> result = future.get(2, TimeUnit.SECONDS);
            if (result.isError()) {
                throw result.error();
            }
            return result.getOrFailsafe(List.of());
        });
    }

    private static Callable<Result<List<Cert>>> getResultCallable(String domain, int port, SSLContext sslContext) {
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        final SSLParameters parameters = new SSLParameters();

        return () -> Result.from(() -> {
            try (final SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(domain, port)) {
                // set up ssl output from configuration
                sslSocket.setUseClientMode(true);
                sslSocket.setSSLParameters(parameters);

                // begin the handshake
                sslSocket.startHandshake();

                // read ssl data
                Certificate[] certificates = sslSocket.getSession().getPeerCertificates();
                return Arrays.stream(certificates)
                        .sequential()
                        .filter(certificate -> certificate instanceof X509Certificate)
                        .map(X509Certificate.class::cast)
                        .map(Cert::fromX509)
                        .toList();
            }
        });
    }
}
