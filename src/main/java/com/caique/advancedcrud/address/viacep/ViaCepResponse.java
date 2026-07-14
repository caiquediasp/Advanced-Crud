package com.caique.advancedcrud.address.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        @JsonProperty("erro") Boolean erro
) {
}
