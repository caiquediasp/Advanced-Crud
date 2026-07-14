package com.caique.advancedcrud.address;

import com.caique.advancedcrud.address.dto.AddressResponse;
import com.caique.advancedcrud.address.dto.CreateAddressRequest;
import com.caique.advancedcrud.address.dto.LookupResponse;
import com.caique.advancedcrud.address.dto.UpdateAddressRequest;
import com.caique.advancedcrud.address.viacep.ViaCepClient;
import com.caique.advancedcrud.address.viacep.ViaCepResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;
    private final ViaCepClient viaCepClient;

    public AddressController(AddressService addressService, ViaCepClient viaCepClient) {
        this.addressService = addressService;
        this.viaCepClient = viaCepClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody CreateAddressRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return addressService.create(userId, request);
    }

    @GetMapping
    public Page<AddressResponse> listMyAddresses(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return addressService.listMyAddresses(userId, pageable);
    }

    @GetMapping("/{publicId}")
    public AddressResponse getOne(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID publicId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return addressService.getOne(userId, publicId);
    }

    @PutMapping("/{publicId}")
    public AddressResponse update(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID publicId,
                                  @Valid @RequestBody UpdateAddressRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return addressService.update(userId, publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt,
                       @PathVariable UUID publicId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        addressService.delete(userId, publicId);
    }

    @PatchMapping("/{publicId}/primary")
    public AddressResponse setPrimary(@AuthenticationPrincipal Jwt jwt,
                                      @PathVariable UUID publicId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return addressService.setPrimary(userId, publicId);
    }

    @GetMapping("/lookup/{cep}")
    public LookupResponse lookup(@PathVariable @Pattern(regexp = "\\d{8}") String cep) {
        ViaCepResponse viaCep = viaCepClient.getAddress(cep);
        return new LookupResponse(
                viaCep.cep().replace("-", ""),
                viaCep.logradouro(),
                viaCep.bairro(),
                viaCep.localidade(),
                viaCep.uf()
        );
    }

}
