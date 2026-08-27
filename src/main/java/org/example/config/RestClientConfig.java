package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestClientConfig {

    @Value("${backend.url}")
    private String backendURL;

    @Bean
    public RestClient financeMVCClientConfig() {

        return RestClient.builder()
                .baseUrl(backendURL)
                .requestInterceptor((request, body, execution) -> {

                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes)
                                    RequestContextHolder.getRequestAttributes();

                    if (attributes != null) {

                        String authorization =
                                attributes.getRequest()
                                        .getHeader(HttpHeaders.AUTHORIZATION);

                        if (authorization != null) {
                            request.getHeaders().set(
                                    HttpHeaders.AUTHORIZATION,
                                    authorization
                            );
                        }
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}
