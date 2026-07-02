package com.caique.AdvancedCrud.address.mapper;

import com.caique.AdvancedCrud.address.Address;
import com.caique.AdvancedCrud.address.dto.AddressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponse toResponse(Address address);

}
