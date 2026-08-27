package org.example.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class KafkaTrustStoreEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            SpringApplication application) {

        try {

            File tempFile = File.createTempFile("kafka-truststore", ".p12");
            tempFile.deleteOnExit();

            try (InputStream in = getClass()
                    .getClassLoader()
                    .getResourceAsStream("certs/kafka-truststore.p12");
                 FileOutputStream out = new FileOutputStream(tempFile)) {

                if (in == null) {
                    throw new IllegalStateException(
                            "kafka-truststore.p12 not found in resources");
                }

                in.transferTo(out);
            }

            MapPropertySource propertySource =
                    new MapPropertySource(
                            "kafkaTrustStore",
                            Map.of(
                                    "spring.kafka.properties.ssl.truststore.location",
                                    tempFile.getAbsolutePath()
                            )
                    );

            environment.getPropertySources().addFirst(propertySource);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Kafka truststore", e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}