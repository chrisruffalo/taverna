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

public class DomainLoader extends BaseLoader<DomainLoaderConfig> {

    @Override
    public List<Cert> load(DomainLoaderConfig configuration) {

        final String domain = configuration.domainName();
        final int port = configuration.port();

        final Result<List<Cert>> serverCertsResult = loadCert(domain, port);
        if (serverCertsResult.isEmpty()) {
            return List.of();
        }

        return serverCertsResult.get();
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

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            try (final SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(domain, port)) {
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
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            return List.of();
        });
    }
}
