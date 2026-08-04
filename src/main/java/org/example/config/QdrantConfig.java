package org.example.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {
    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.port}")
    private int port;

    @Value("${qdrant.api-key}")
    private String apiKey;

    @Bean
    public QdrantClient qdrantClient(){
        QdrantGrpcClient qdrantGrpcClient=QdrantGrpcClient
                .newBuilder(host,port,true)
                .withApiKey(apiKey)
                .build();

        return new QdrantClient(qdrantGrpcClient);
    }


}
