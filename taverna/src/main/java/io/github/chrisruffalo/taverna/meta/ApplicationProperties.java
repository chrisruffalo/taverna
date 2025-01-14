package io.github.chrisruffalo.taverna.meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {

    private enum Singleton {
        INSTANCE;

        private final Properties properties;

        Singleton() {
            try (final InputStream propStream = ApplicationProperties.class.getResourceAsStream("/application.properties")) {
                this.properties = new Properties();
                properties.load(propStream);
            } catch (IOException e) {
                throw new RuntimeException("could not load properties file: " + e.getMessage(), e);
            }
        }

        public String get(String key) {
            return properties.getProperty(key);
        }
    }

    public static String version() {
        return Singleton.INSTANCE.get("version");
    }

}
