package io.github.chrisruffalo.taverna.pki.dir;

import io.github.chrisruffalo.taverna.pki.LoaderConfig;

import java.nio.file.Path;

public record DirLoaderConfig(Path dir) implements LoaderConfig {

}
