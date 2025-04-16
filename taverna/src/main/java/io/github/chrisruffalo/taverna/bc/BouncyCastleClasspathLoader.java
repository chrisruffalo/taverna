package io.github.chrisruffalo.taverna.bc;

import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.opt.Options;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Provider;

public class BouncyCastleClasspathLoader implements BouncyCastleLoader {

    public Result<Provider> getBouncyCastleFipsProvider(Options options) {
        return getProviderResult(options, BCFIPS_JAR, BCFIPS_NAME);
    }

    public Result<Provider> getBouncyCastleProvider(Options options) {
        return getProviderResult(options, BC_JAR, BC_NAME);
    }

    private Result<Provider> getProviderResult(Options options, String jarName, String bcName) {
        final URLClassLoader classLoader = getClassLoader(jarName);
        return Result.from(() -> {
            final Class<?> foundClass = classLoader.loadClass(bcName);
            return (Provider) foundClass.getDeclaredConstructor().newInstance();
        });
    }

    static URLClassLoader getClassLoader(final String jarName) {
        final String resourceName = String.format("bc/%s", jarName);
        URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new RuntimeException("No jar resource found included");
        }
        if (resource.toString().startsWith("jar:")) {
            try {
                resource = extract(resourceName, jarName).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad url from extract", e);
            }
        }
        return new URLClassLoader(new URL[]{resource}, null);
    }

    static synchronized Path extract(final String resourceName, final String jarName) {
        final Path tmp = Paths.get(System.getProperty("java.io.tmpdir"));
        final String extractedJar = String.format("extracted-%s", jarName);
        final Path extracted = tmp.resolve(extractedJar);
        if (Files.exists(extracted)) {
            return extracted;
        }
        try (
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            OutputStream os = Files.newOutputStream(extracted);
        ) {
            if (is == null) {
                throw new RuntimeException("could not open jar resource: " + resourceName);
            }
            is.transferTo(os);
        } catch (Exception ex) {
            throw new RuntimeException("could not extract jar", ex);
        }
        return extracted;
    }

}
