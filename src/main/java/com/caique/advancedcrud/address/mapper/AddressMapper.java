package com.caique.advancedcrud.address.mapper;

import com.caique.advancedcrud.address.Address;
import com.caique.advancedcrud.address.dto.AddressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponse toResponse(Address address);

}
