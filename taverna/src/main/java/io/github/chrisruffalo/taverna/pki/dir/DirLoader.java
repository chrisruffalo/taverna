package io.github.chrisruffalo.taverna.pki.dir;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.model.Cert;
import io.github.chrisruffalo.taverna.pki.BaseLoader;
import io.github.chrisruffalo.taverna.pki.file.FileLoader;
import io.github.chrisruffalo.taverna.pki.file.FileLoaderConfig;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DirLoader extends BaseLoader<DirLoaderConfig> {

    @Override
    public Result<List<Cert>> load(DirLoaderConfig configuration) {
        final Path path = configuration.dir();
        if (path == null || !Files.isDirectory(path)) {
            return Result.empty();
        }

        final Set<Path> toLoad = new HashSet<>();

        try {
            Files.walkFileTree(path, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file)) {
                        toLoad.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            return Result.of(null, e);
        }

        final FileLoader loader = new FileLoader();
        return Result.from(() -> toLoad.stream()
                .flatMap(loading -> {
                    final FileLoaderConfig loaderConfig = new FileLoaderConfig(loading);
                    return loader.load(loaderConfig).getOrFailsafe(List.of()).stream();
                })
                .collect(Collectors.toList()));
    }

}
