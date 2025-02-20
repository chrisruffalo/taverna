package io.github.chrisruffalo.taverna.pki.validate;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.error.Codes;
import io.github.chrisruffalo.taverna.log.OutputLogger;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.opt.Options;
import io.github.chrisruffalo.taverna.pki.combine.Combiner;
import io.github.chrisruffalo.taverna.pki.domain.DomainLoader;
import io.github.chrisruffalo.taverna.pki.domain.DomainLoaderConfig;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that encapsulates the logic of validating the loaded trust
 * against the desired endpoints.
 */
public class Validator {

    public Result<ValidationStatus> validate(OutputLogger logger, Options options, List<String> domains, List<Cert> trust) {
         return Result.from(() -> {

             final ValidationStatus status = new ValidationStatus();

             // combine keystore
             final KeyStore combined = Combiner.combineTrust(trust);

             // create thumbprint map
             final Map<String, Cert> certThumbprintMap = new HashMap<>();
             final Map<String, List<Cert>> certSubjectMap = new HashMap<>();
             trust.forEach(cert -> {
                 // the subject map can contain multiple of the same subject and we need to try and
                 // find/figure the right one
                 certSubjectMap.computeIfAbsent(cert.getSubject(), key -> new LinkedList<>()).add(cert);
                 certThumbprintMap.put(cert.getThumbprint(), cert);
             });

             // for each domain, load
             final DomainLoader domainLoader = new DomainLoader();
             final AtomicInteger updatedCode = new AtomicInteger(Codes.OK);

             domains.forEach(domain -> {
                 final DomainLoaderConfig domainLoaderConfig = new DomainLoaderConfig(domain, Options.DEFAULT_SSL_PORT);
                 final Result<List<Cert>> fromDomainResult = domainLoader.load(domainLoaderConfig);
                 final String domainName = domainLoaderConfig.getDomainName();
                 final int domainPort = domainLoaderConfig.getDomainPort();
                 List<Cert> fromDomain = List.of();
                 if (fromDomainResult.isError()) {
                     logger.errorf("could not get certificates from %s:%s:", domainName, domainPort);
                     final Exception error = fromDomainResult.error();
                     logger.errorf("\terror: %s", error.getMessage());
                 } else if (fromDomainResult.isEmpty()) {
                     logger.infof("no certificates loaded from %s:%s:");
                 } else {
                     logger.infof("certificate chain from %s%s:", domainName, domainPort);
                     fromDomain = fromDomainResult.get();
                     final Cert domainFirstCert = fromDomain.getFirst();
                     boolean hostnameVerified = HostnameVerifier.verifyHostname(domainName, domainFirstCert.getAlternateNames());
                     if (hostnameVerified) {
                         logger.infof("\thostname '%s' verified", domainName);
                     } else {
                         logger.infof("\thostname not verified: [%s] against [%s]", domainName, String.join(", ", domainFirstCert.getAlternateNames()));
                     }
                 }

                 final AtomicBoolean alreadyAdded = new AtomicBoolean(false);

                 fromDomain.forEach(domainCert -> {
                     boolean inStore = certThumbprintMap.containsKey(domainCert.getThumbprint());

                     final List<String> qualifiers = new ArrayList<>();
                     if (inStore) {
                         qualifiers.add("(in store)");
                         status.getUsedCerts().add(certThumbprintMap.get(domainCert.getThumbprint()));
                         alreadyAdded.set(true);
                     }
                     if (domainCert.isSelfSigned()) {
                         qualifiers.add("(self-signed)");
                     }

                     // get the issuer, if it is in the map
                     final List<Cert> knownDomainIssuers = certSubjectMap.computeIfAbsent(domainCert.getIssuer(), key -> List.of());
                     final Cert knownDomainIssuer = knownDomainIssuers.stream()
                             .filter(Objects::nonNull)
                             .filter(domainCert::isIssuedBy)
                             .findFirst()
                             .orElse(null);
                     final boolean issuerInTrust = knownDomainIssuer != null;

                     logger.infof("\t[serial=%s] %s [%s] [issuer=%s, %s, %s] %s", domainCert.getSerialNumber(), domainCert.getSubject(), domainCert.getThumbprint(), domainCert.getIssuer(), knownDomainIssuer != null ? "in trust" : "not in trust", issuerInTrust ? "trusted" : "not trusted", String.join(" ", qualifiers));
                 });

                 // determine if the trust package trusts the connection (offline)
                 final Result<Boolean> trustedByManagerResult = validateDomainCertsWithTrustManager(combined, trust, fromDomain);
                 boolean trustedByManager = trustedByManagerResult.getOrFailsafe(false);
                 if (!trustedByManager) {
                     logger.infof("\tnot trusted%s", trustedByManagerResult.isError() ? String.format(": %s", trustedByManagerResult.error().getMessage()) : "");
                     updatedCode.set(Codes.UNVERIFIED);
                 } else {
                     System.out.println("\ttrusted");
                 }

                 // determine where the chain is trusted
                 final Result<Cert> trustedResult = validateDomainCertificatePath(combined, trust, fromDomain);
                 boolean chainIsTrusted = trustedResult.isPresent();
                 if (chainIsTrusted) {
                     final Cert trustedCert = trustedResult.get();
                     if (certSubjectMap.containsKey(trustedCert.getIssuer())) {
                         final List<Cert> potentialIssuers = certSubjectMap.get(trustedCert.getIssuer());
                         for (final Cert issuer : potentialIssuers) {
                             if (trustedCert.isIssuedBy(issuer)) {
                                 logger.infof("\tanchored by: %s", issuer);
                                 if (!alreadyAdded.get()) {
                                     status.getUsedCerts().add(issuer);
                                 }
                                 break;
                             }
                         }
                     }
                 }

                 // only attempt if not toggled off and the chain is trusted/trusted by manager
                 if (!options.isNoVerify() && (chainIsTrusted || trustedByManager)) {
                     Result<Boolean> confirmed = validateDomainConnectionAgainstTrust(domainName, domainPort, trust);
                     if (confirmed.isPresent()) {
                         System.out.println("\tverified trusted connection");
                     } else if (confirmed.isError()) {
                         updatedCode.set(Codes.UNVERIFIED);
                         logger.infof("\ttrusted connection not verified: %s", confirmed.error().getMessage());
                     } else {
                         updatedCode.set(Codes.UNVERIFIED);
                         System.out.println("\ttrusted connection not verified");
                     }
                 }

                 // if we are in a completion mode and not trusted
                 if (options.isComplete() && !(chainIsTrusted || trustedByManager)) {
                    final Cert toTrust = switch (options.getCompletionMode()) {
                        case MOST_TRUSTED -> fromDomain.getLast();
                        case FIRST_SUBORDINATE -> {
                            if (fromDomain.size() > 1) {
                                yield fromDomain.get(1);
                            }
                            yield fromDomain.getLast();
                        }
                        case null, default -> fromDomain.getFirst();
                    };
                    status.getMissingCerts().add(toTrust);
                    logger.infof("\tadding to trusted material: %s", toTrust);

                    if (!options.isNoVerify()) {
                        Result<Boolean> confirmed = validateDomainConnectionAgainstTrust(domainName, domainPort, List.of(toTrust));
                        if (confirmed.isPresent()) {
                            updatedCode.set(Codes.OK);
                            System.out.println("\tconnection verified with updated trust");
                        } else if (confirmed.isError()) {
                            updatedCode.set(Codes.UNVERIFIED);
                            logger.infof("\tupdated trust not verified: %s", confirmed.error().getMessage());
                        } else {
                            updatedCode.set(Codes.UNVERIFIED);
                            System.out.println("\tupdated trust not verified");
                        }
                    }
                 }

                 // update the status/output code if it has changed for the worse, first
                 // bad code "wins"
                 if (status.getReturnCode() == Codes.OK && updatedCode.get() != Codes.OK) {
                     status.setReturnCode(updatedCode.get());
                 }
             });

             return status;
         });
     }

     Result<Boolean> validateDomainCertsWithTrustManager(final KeyStore combined, final List<Cert> trust, final List<Cert> domainCert) {
        if (combined == null || trust == null || trust.isEmpty()) {
            return Result.empty();
        }
        return Result.from(() -> {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(combined);

            // pretty sure this should only result in 1 at this point
            final List<X509TrustManager> trustManagers = Arrays.stream(tmf.getTrustManagers())
                    .filter(tm -> tm instanceof X509TrustManager)
                    .map(X509TrustManager.class::cast)
                    .toList();
            if (trustManagers.size() > 1) {
                System.err.printf("unexpected number of trust managers found: %s", trustManagers.size());
            }
            final X509TrustManager trustManager = trustManagers.getFirst();

            // get trust checking algorithm
            final String serverCertAlg = domainCert.getFirst().getOriginal().getPublicKey().getAlgorithm();
            String authType = "RSA";
            if (serverCertAlg.equals("EC")) {
                authType = "ECDHE_ECDSA";
            }

            // build x509 chain
            X509Certificate[] chain = domainCert.stream()
                    .map(Cert::getOriginal)
                    .filter(cert -> cert instanceof X509Certificate)
                    .map(X509Certificate.class::cast)
                    .toArray(X509Certificate[]::new);

            // validate (throws exception if not validated)
            trustManager.checkServerTrusted(chain, authType);

            return true;
        });
     }

     Result<Cert> validateDomainCertificatePath(final KeyStore combined, final List<Cert> trust, final List<Cert> fromDomain) {
        if (combined == null || trust == null || trust.isEmpty()) {
            return Result.empty();
        }
        final List<Cert> tempChain = new ArrayList<>(fromDomain);
        do {
            final Result<Cert> step = Result.from(() -> {
                final CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
                final PKIXParameters pkixParams = new PKIXParameters(combined);
                pkixParams.setRevocationEnabled(false);

                // create certificate factory
                final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

                CertPath path = certFactory.generateCertPath(tempChain.stream().map(Cert::getOriginal).toList());
                certPathValidator.validate(path, pkixParams);
                return tempChain.getLast();
            });
            if (step.isPresent()) {
                return step;
            }
            // bite off last step
            tempChain.removeLast();
        } while (!tempChain.isEmpty());

        return Result.empty();
     }

     Result<Boolean> validateDomainConnectionAgainstTrust(final String domain, final int port, List<Cert> trustedCerts) {
        return Result.from(() -> {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // this uses its own keystore and trust in case we are trying to build a new
            // view of the trust just for this connection (like when we try to "build" or
            // "repair" trust)
            final KeyStore combined = Combiner.combineTrust(trustedCerts);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(combined);

            // Get the TrustManagers
            sslContext.init(null, tmf.getTrustManagers(), null);

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            try (final SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(domain, port)) {
                // begin the handshake
                sslSocket.startHandshake();
                // trust
                return true;
            }
        });
     }

}
