package io.github.chrisruffalo.taverna.cmd;

import com.beust.jcommander.JCommander;
import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.error.Codes;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.model.StoreType;
import io.github.chrisruffalo.taverna.opt.OptGlobal;
import io.github.chrisruffalo.taverna.pki.combine.Combiner;
import io.github.chrisruffalo.taverna.pki.dir.DirLoader;
import io.github.chrisruffalo.taverna.pki.dir.DirLoaderConfig;
import io.github.chrisruffalo.taverna.pki.file.FileLoader;
import io.github.chrisruffalo.taverna.pki.file.FileLoaderConfig;
import io.github.chrisruffalo.taverna.pki.store.TrustStoreLoader;
import io.github.chrisruffalo.taverna.pki.store.TrustStoreLoaderConfig;
import io.github.chrisruffalo.taverna.pki.validate.ValidationStatus;
import io.github.chrisruffalo.taverna.pki.validate.Validator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Entrypoint class
 */
public class Entrypoint {

    public static void main(String[] args) {
        final OptGlobal optGlobal = new OptGlobal();
        final JCommander jCommander = JCommander
                .newBuilder()
                .addObject(optGlobal)
                .build();
        jCommander.parse(args);

        final Entrypoint entrypoint = new Entrypoint();
        final int code = entrypoint.run(optGlobal);
        if (code != 0) {
            System.exit(code);
        }
    }

    public int run(final OptGlobal optGlobal) {
        // load domains to check trust against
        final List<String> domains = new LinkedList<>(optGlobal.getDomains());
        optGlobal.getDomainFiles().forEach(domainFile -> {
            final Path domainFilePath = Paths.get(domainFile.getPath());
            if (Files.exists(domainFilePath)) {
                domains.addAll(Result.from(() -> Files.readAllLines(domainFilePath)).getOrFailsafe(List.of()));
            }
        });

        // load trusts
        final List<Cert> certs = new LinkedList<>();
        final DirLoader dirLoader = new DirLoader();
        final FileLoader fileLoader = new FileLoader();
        final TrustStoreLoader trustStoreLoader = new TrustStoreLoader();

        optGlobal.getSource().forEach(source -> {
            if (source == null || source.isEmpty()) {
                return;
            }

            Path asPath = Paths.get(source);
            String storePass = optGlobal.getStorePass();
            List<Cert> loaded = List.of();

            // there is a store pass being offered as part of the path
            if (source.contains(":")) {
                final String[] split = source.split(":");
                if (split.length >= 2) {
                    asPath = Paths.get(split[0]);
                    storePass = split[1];
                }
            }

            if (Files.isDirectory(asPath)) {
                final DirLoaderConfig dirLoaderConfig = new DirLoaderConfig(asPath);
                loaded = dirLoader.load(dirLoaderConfig);
                System.out.printf("loaded %s certificates from directory %s\n", loaded.size(), asPath);
                certs.addAll(loaded);
            }

            if (Files.isRegularFile(asPath)) {
                FileLoaderConfig fileLoaderConfig = new FileLoaderConfig(asPath);
                loaded = fileLoader.load(fileLoaderConfig);
                if (!loaded.isEmpty()) {
                    System.out.printf("loaded %s certificates from file %s\n", loaded.size(), asPath);
                } else {
                    // attempt to load as trust store
                    for (StoreType storeType : StoreType.values()) {
                        final String storeTypeName = storeType.name().toLowerCase();
                        final TrustStoreLoaderConfig trustStoreLoaderConfig = new TrustStoreLoaderConfig(asPath, storeTypeName, storePass);
                        loaded = trustStoreLoader.load(trustStoreLoaderConfig);
                        if (!loaded.isEmpty()) {
                            System.out.printf("loaded %s certificates from truststore %s\n", loaded.size(), asPath);
                            break;
                        }
                    }
                }
                certs.addAll(loaded);
            }

            loaded.forEach(individualLoadedCert -> {
                System.out.printf("\t[serial=%s] %s [%s] [issuer=%s]\n", individualLoadedCert.getSerialNumber(), individualLoadedCert.getSubject(), individualLoadedCert.getThumbprint(), individualLoadedCert.getIssuer());
            });

        });

        System.out.printf("loaded %d total certificates\n", certs.size());

        // stop here if no domains were added (can't simplify, maybe in the future we want to allow output)
        if (domains.isEmpty() && !optGlobal.isNoDomains()) {
            System.err.printf("No domains to verify trust against, exit code %d\n", Codes.NO_DOMAINS);
            return Codes.NO_DOMAINS;
        }

        // for simplicity's sake the trust is finalized after loading
        // and will be further refined if domain checks are enabled.
        final Set<Cert> finalizedTrust = new HashSet<>(certs);

        // if we are acknowledging that there are no domains
        // being provided then we can skip domain verification
        // and go straight to output for cert/trust store management.
        // this implies no simplification or trust completion will
        // be performed.
        int validationResultCode = 0;
        if (!optGlobal.isNoDomains()) {
            final Validator validator = new Validator();
            final Result<ValidationStatus> validationStatusResult = validator.validate(optGlobal, domains, certs);
            if (validationStatusResult.isError()) {
                return Codes.GENERAL_ERROR;
            }
            ValidationStatus validationStatus = validationStatusResult.get();

            if (optGlobal.isComplete() && !optGlobal.isSimplify()) {
                finalizedTrust.addAll(validationStatus.getMissingCerts());
            }
            if (optGlobal.isSimplify()) {
                finalizedTrust.clear();
                finalizedTrust.addAll(validationStatus.getUsedCerts());
                if (optGlobal.isComplete()) {
                    finalizedTrust.addAll(validationStatus.getMissingCerts());
                }
                // show finalized trust
                if (finalizedTrust.isEmpty()) {
                    System.out.println("no trusted certificates remain after simplification");
                } else {
                    System.out.printf("simplified trust (%d entries):\n", finalizedTrust.size());
                    finalizedTrust.forEach(cert -> {
                        System.out.printf("\t%s\n", cert);
                    });
                }
            }

            // set return code
            validationResultCode = validationStatus.getReturnCode();
        }

        // only output if all the domains are valid
        if (validationResultCode == Codes.OK) {
            if (optGlobal.getOutstore() != null) {
                final Path outStorePath = optGlobal.getOutstore();
                if (Files.isDirectory(outStorePath)) {
                    System.out.printf("the output store path ('%s') is a directory\n", outStorePath);
                } else {
                    final String outStorePass = optGlobal.getOutStorePass();
                    if (outStorePass == null || outStorePass.isEmpty()) {
                        System.out.println("the output store pass is empty, skipping");
                    } else {
                        if (OptGlobal.DEFAULT_STORE_PASSWORD.equals(outStorePass)) {
                            System.out.println("WARNING: the output store pass is the default password, this is insecure and it should be changed");
                        }
                        final KeyStore finalKeyStore = Combiner.combineTrust(finalizedTrust);
                        try (OutputStream os = Files.newOutputStream(outStorePath)) {
                            finalKeyStore.store(os, outStorePass.toCharArray());
                            System.out.printf("wrote trust store to '%s'\n", outStorePath);
                        } catch (Exception ex) {
                            System.err.printf("failed to create trust store at '%s': %s\n", outStorePath, ex.getMessage());
                        }
                    }
                }
            }

            if (optGlobal.getOutdir() != null) {
                final Path outDirPath = optGlobal.getOutdir();
                if (Files.isRegularFile(outDirPath)) {
                    System.out.printf("the output dir ('%s') is not a directory\n", outDirPath);
                } else if (!Files.exists(outDirPath)) {
                    try {
                        Files.createDirectories(outDirPath);
                    } catch (Exception ex) {
                        // what to even do here
                    }
                }
                System.out.printf("writing %d certificates to %s\n", finalizedTrust.size(), outDirPath);
                finalizedTrust.forEach(cert -> {
                    final Path individualCertPath = optGlobal.getOutdir().resolve(cert.getSubject() + ".pem");
                    try (final OutputStream fileOutputStream = Files.newOutputStream(individualCertPath)) {
                        fileOutputStream.write(cert.getPemEncoded().getBytes(StandardCharsets.UTF_8));
                        System.out.printf("\twrote %s to '%s'\n", cert, individualCertPath);
                    } catch (Exception ex) {
                        System.err.printf("\tcould not right certificate %s to %s\n", cert.getSubject(), individualCertPath);
                    }
                });
            }

            if (optGlobal.getOutfile() != null) {
                final Path outFilePath = optGlobal.getOutfile();
                if (Files.isDirectory(outFilePath)) {
                    System.err.printf("the output store path ('%s') is a directory\n", outFilePath);
                } else {
                    try (OutputStream os = Files.newOutputStream(outFilePath)) {
                        boolean first = true;
                        for (final Cert cert : finalizedTrust) {
                            if (!first) {
                                os.write('\n');
                            }
                            os.write(cert.getPemEncoded().getBytes(StandardCharsets.UTF_8));
                            first = false;
                        }
                        System.out.printf("wrote %d certificates to file '%s'\n", finalizedTrust.size(), outFilePath);
                    } catch (Exception ex) {
                        System.err.printf("failed to write certificates to file '%s': %s\n", outFilePath, ex.getMessage());
                    }
                }
            }
        } else if (optGlobal.getOutdir() != null || optGlobal.getOutfile() != null || optGlobal.getOutstore() != null) {
            System.err.println("there was an error during domain validation, no certificate output was written");
        }

        // return result code
        return validationResultCode;
    }

}
