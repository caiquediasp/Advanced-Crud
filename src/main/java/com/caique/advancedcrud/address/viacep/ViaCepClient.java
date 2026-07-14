package com.caique.advancedcrud.address.viacep;

import com.caique.advancedcrud.shared.exceptions.CepNotFoundException;
import com.caique.advancedcrud.shared.exceptions.CepServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ViaCepClient {

    private final RestClient viaCepRestClient;

    public ViaCepClient(RestClient viaCepRestClient) {
        this.viaCepRestClient = viaCepRestClient;
    }

    @Cacheable(value = "ceps", key = "#cep")
    @CircuitBreaker(name = "viacep", fallbackMethod = "fallback")
    @Retry(name = "viacep")
    public ViaCepResponse getAddress(String cep) {
        ViaCepResponse response = viaCepRestClient.get()
                .uri("/{cep}/json/", cep)
                .retrieve()
                .body(ViaCepResponse.class);

        if (response == null || Boolean.TRUE.equals(response.erro())) {
            throw new CepNotFoundException(cep);
        }

        return response;
    }

    private ViaCepResponse fallback(String cep, Throwable t) {
        if (t instanceof CepNotFoundException) {
            throw (CepNotFoundException) t;
        }

        throw new CepServiceUnavailableException();
    }

}
