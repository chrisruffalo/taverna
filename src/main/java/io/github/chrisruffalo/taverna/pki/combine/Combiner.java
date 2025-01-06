package io.github.chrisruffalo.taverna.pki.combine;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;

import java.security.KeyStore;
import java.util.Collection;

public class Combiner {

    public static KeyStore combineTrust(Collection<Cert> certs) {
        return Result.from(() -> {
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
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
