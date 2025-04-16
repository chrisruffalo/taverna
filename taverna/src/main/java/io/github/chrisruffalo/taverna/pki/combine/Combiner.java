package io.github.chrisruffalo.taverna.pki.combine;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collection;

/**
 * Given a set of certificate objects this combiner
 * will create a single key store with all the certificates
 * inside it.
 */
public class Combiner {

    private static final String DEFAULT_TYPE = "PKCS12";

    public static KeyStore combineTrust(KeyStore into, Collection<Cert> certs) {
        return Result.from(() -> {
                    into.load(null, null);

            for(Cert cert : certs) {
                final String alias = cert.getSubject();

                // check for alias in keystore
                if (into.containsAlias(alias)) {
                    continue;
                }

                // add certificate that doesn't exist
                into.setCertificateEntry(cert.getSubject(), cert.getOriginal());
            }

            return into;
        })
        .recover(e -> {
            System.out.println(e.getMessage());
            return null;
        })
        .getOrFailsafe(null);
    }

    public static KeyStore combineTrust(Collection<Cert> certs) {
        return Result.from(() -> combineTrust(KeyStore.getInstance(DEFAULT_TYPE), certs)).getOrFailsafe(null);
    }

}
