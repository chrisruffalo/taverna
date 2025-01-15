package io.github.chrisruffalo.taverna.pki.combine;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.log.OutputLogger;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.model.StoreType;
import io.github.chrisruffalo.taverna.opt.Options;
import io.github.chrisruffalo.taverna.pki.dir.DirLoader;
import io.github.chrisruffalo.taverna.pki.dir.DirLoaderConfig;
import io.github.chrisruffalo.taverna.pki.file.FileLoader;
import io.github.chrisruffalo.taverna.pki.file.FileLoaderConfig;
import io.github.chrisruffalo.taverna.pki.store.TrustStoreLoader;
import io.github.chrisruffalo.taverna.pki.store.TrustStoreLoaderConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Loader {

    public List<Cert> load(Options options, OutputLogger logger) {
        // load trusts
        final List<Cert> certs = new LinkedList<>();
        final DirLoader dirLoader = new DirLoader();
        final FileLoader fileLoader = new FileLoader();
        final TrustStoreLoader trustStoreLoader = new TrustStoreLoader();

        options.getSource().forEach(source -> {
            if (source == null || source.isEmpty()) {
                return;
            }

            Path asPath = Paths.get(source);
            String storePass = options.getStorePass();
            List<Cert> loaded = new ArrayList<>(0);

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
                Result<List<Cert>> dirResult = dirLoader.load(dirLoaderConfig);
                if (dirResult.isEmpty()) {
                    logger.infof("no certificates loaded from directory %s", asPath);
                } else if(dirResult.isError()) {
                    logger.errorf("error while loading certificates from directory %s: %s", asPath, dirResult.error().getMessage());
                } else {
                    loaded.addAll(dirResult.get());
                    logger.infof("loaded %s certificates from directory %s", loaded.size(), asPath);
                    certs.addAll(loaded);
                }
            }

            if (Files.isRegularFile(asPath)) {
                FileLoaderConfig fileLoaderConfig = new FileLoaderConfig(asPath);
                Result<List<Cert>> fileLoaderResult = fileLoader.load(fileLoaderConfig);

                if (!fileLoaderResult.isEmpty()) {
                    loaded.addAll(fileLoaderResult.get());
                    logger.infof("loaded %s certificates from file %s", loaded.size(), asPath);
                } else {
                    // attempt to load as trust store
                    for (StoreType storeType : StoreType.values()) {
                        final String storeTypeName = storeType.name().toLowerCase();
                        final TrustStoreLoaderConfig trustStoreLoaderConfig = new TrustStoreLoaderConfig(asPath, storeTypeName, storePass);
                        Result<List<Cert>> keyStoreResult = trustStoreLoader.load(trustStoreLoaderConfig);
                        if (!keyStoreResult.isEmpty()) {
                            loaded.addAll(keyStoreResult.get());
                            logger.infof("loaded %s certificates from truststore %s", loaded.size(), asPath);
                            break;
                        }
                    }
                }
                certs.addAll(loaded);
            }

            loaded.forEach(individualLoadedCert -> {
                logger.infof("\t[serial=%s] %s [%s] [issuer=%s]", individualLoadedCert.getSerialNumber(), individualLoadedCert.getSubject(), individualLoadedCert.getThumbprint(), individualLoadedCert.getIssuer());
            });

        });

        logger.infof("loaded %d total certificates", certs.size());
        return certs;
    }

}
