package io.github.chrisruffalo.taverna.pki.file;

import io.github.chrisruffalo.taverna.pki.LoaderConfig;

import java.nio.file.Path;

public record FileLoaderConfig(Path path) implements LoaderConfig {

}
