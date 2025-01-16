package io.github.chrisruffalo.taverna.opt;

import java.nio.file.Path;
import java.util.List;

public interface Options {
    String DEFAULT_STORE_PASSWORD = "changeit";

    String DEFAULT_STORE_TYPE = "PKCS12";

    int DEFAULT_SSL_PORT = 443;

    List<String> getDomains();

    List<Path> getDomainFiles();

    List<String> getSource();

    String getStorePass();

    Path getOutstore();

    String getOutstoreType();

    String getOutStorePass();

    boolean isSimplify();

    boolean isComplete();

    boolean isNoVerify();

    Path getOutfile();

    Path getOutdir();

    CompletionMode getCompletionMode();

    boolean isNoDomains();
}
