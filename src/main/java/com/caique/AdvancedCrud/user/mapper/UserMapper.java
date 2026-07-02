package com.caique.AdvancedCrud.user.mapper;

import com.caique.AdvancedCrud.user.Role;
import com.caique.AdvancedCrud.user.User;
import com.caique.AdvancedCrud.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    UserResponse toResponse(User user);

    default Set<String> mapRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
