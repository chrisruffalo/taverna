package io.github.chrisruffalo.taverna;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.error.Codes;
import io.github.chrisruffalo.taverna.log.OutputLogger;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.opt.Options;
import io.github.chrisruffalo.taverna.pki.combine.Combiner;
import io.github.chrisruffalo.taverna.pki.combine.Loader;
import io.github.chrisruffalo.taverna.pki.validate.ValidationStatus;
import io.github.chrisruffalo.taverna.pki.validate.Validator;

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

public class Engine {

    final OutputLogger logger;

    final Options options;

    public Engine(OutputLogger logger, Options options) {
        this.logger = logger;
        this.options = options;
    }

    public int run() {
        // load domains to check trust against
        final List<String> domains = new LinkedList<>(options.getDomains());
        options.getDomainFiles().forEach(domainFile -> {
            if (Files.exists(domainFile)) {
                domains.addAll(Result.from(() -> Files.readAllLines(domainFile)).getOrFailsafe(List.of()));
            }
        });

        final Loader combinedLoader = new Loader();
        final List<Cert> certs = combinedLoader.load(options, logger);

        // stop here if no domains were added (can't simplify, maybe in the future we want to allow output)
        if (domains.isEmpty() && !options.isNoDomains()) {
            logger.errorf("No domains to verify trust against, exit code %d", Codes.NO_DOMAINS);
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
        if (!options.isNoDomains()) {
            final Validator validator = new Validator();
            final Result<ValidationStatus> validationStatusResult = validator.validate(logger, options, domains, certs);
            if (validationStatusResult.isError()) {
                return Codes.GENERAL_ERROR;
            }
            ValidationStatus validationStatus = validationStatusResult.get();

            if (options.isComplete() && !options.isSimplify()) {
                finalizedTrust.addAll(validationStatus.getMissingCerts());
            }
            if (options.isSimplify()) {
                finalizedTrust.clear();
                finalizedTrust.addAll(validationStatus.getUsedCerts());
                if (options.isComplete()) {
                    finalizedTrust.addAll(validationStatus.getMissingCerts());
                }
                // show finalized trust
                if (finalizedTrust.isEmpty()) {
                    logger.infof("no trusted certificates remain after simplification");
                } else {
                    logger.infof("simplified trust (%d entries):", finalizedTrust.size());
                    finalizedTrust.forEach(cert -> {
                        logger.infof("\t%s", cert);
                    });
                }
            }

            // set return code
            validationResultCode = validationStatus.getReturnCode();
        }

        // only output if all the domains are valid
        if (validationResultCode == Codes.OK) {
            if (options.getOutstore() != null) {
                final Path outStorePath = options.getOutstore();
                if (Files.isDirectory(outStorePath)) {
                    logger.infof("the output store path ('%s') is a directory", outStorePath);
                } else {
                    final String outStorePass = options.getOutStorePass();
                    if (outStorePass == null || outStorePass.isEmpty()) {
                        logger.infof("the output store pass is empty, skipping");
                    } else {
                        if (Options.DEFAULT_STORE_PASSWORD.equals(outStorePass)) {
                            logger.infof("WARNING: the output store pass is the default password, this is insecure and it should be changed");
                        }
                        final KeyStore finalKeyStore = Combiner.combineTrust(finalizedTrust);
                        try (OutputStream os = Files.newOutputStream(outStorePath)) {
                            finalKeyStore.store(os, outStorePass.toCharArray());
                            logger.infof("wrote trust store to '%s'", outStorePath);
                        } catch (Exception ex) {
                            logger.errorf("failed to create trust store at '%s': %s", outStorePath, ex.getMessage());
                        }
                    }
                }
            }

            if (options.getOutdir() != null) {
                final Path outDirPath = options.getOutdir();
                if (Files.isRegularFile(outDirPath)) {
                    logger.infof("the output dir ('%s') is not a directory", outDirPath);
                } else if (!Files.exists(outDirPath)) {
                    try {
                        Files.createDirectories(outDirPath);
                    } catch (Exception ex) {
                        // what to even do here
                    }
                }
                logger.infof("writing %d certificates to %s", finalizedTrust.size(), outDirPath);
                finalizedTrust.forEach(cert -> {
                    final Path individualCertPath = options.getOutdir().resolve(cert.getSubject() + ".pem");
                    try (final OutputStream fileOutputStream = Files.newOutputStream(individualCertPath)) {
                        fileOutputStream.write(cert.getPemEncoded().getBytes(StandardCharsets.UTF_8));
                        logger.infof("\twrote %s to '%s'", cert, individualCertPath);
                    } catch (Exception ex) {
                        logger.errorf("\tcould not right certificate %s to %s", cert.getSubject(), individualCertPath);
                    }
                });
            }

            if (options.getOutfile() != null) {
                final Path outFilePath = options.getOutfile();
                if (Files.isDirectory(outFilePath)) {
                    logger.errorf("the output store path ('%s') is a directory", outFilePath);
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
                        logger.infof("wrote %d certificates to file '%s'", finalizedTrust.size(), outFilePath);
                    } catch (Exception ex) {
                        logger.errorf("failed to write certificates to file '%s': %s", outFilePath, ex.getMessage());
                    }
                }
            }
        } else if (options.getOutdir() != null || options.getOutfile() != null || options.getOutstore() != null) {
            logger.errorf("there was an error during domain validation, no certificate output was written");
        }

        // return result code
        return validationResultCode;
    }

}
