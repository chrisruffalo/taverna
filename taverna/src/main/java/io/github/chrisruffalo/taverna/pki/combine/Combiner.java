package io.github.chrisruffalo.taverna.pki.combine;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;

import java.security.KeyStore;
import java.util.Collection;

/**
 * Given a set of certificate objects this combiner
 * will create a single key store with all the certificates
 * inside it.
 */
public class Combiner {

    private static final String DEFAULT_TYPE = "PKCS12";

    public static KeyStore combineTrust(Collection<Cert> certs) {
        return Result.from(() -> {
            final KeyStore keyStore = KeyStore.getInstance(DEFAULT_TYPE);
            keyStore.load(null, null);

            for(Cert cert : certs) {
                final String alias = cert.getSubject();

                // check for alias in keystore
                if (keyStore.containsAlias(alias)) {
                    continue;
                }

                // add certificate that doesn't exist
                keyStore.setCertificateEntry(cert.getSubject(), cert.getOriginal());
            }

            return keyStore;
        })
        .recover(e -> {
            System.out.println(e.getMessage());
            return null;
        })
        .getOrFailsafe(null);
    }

}
