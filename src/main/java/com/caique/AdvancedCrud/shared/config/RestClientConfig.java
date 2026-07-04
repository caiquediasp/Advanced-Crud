package com.caique.AdvancedCrud.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient viaCepRestClient() {
        return RestClient.builder()
                .baseUrl("https://viacep.com.br/ws")
                .build();
    }
}
