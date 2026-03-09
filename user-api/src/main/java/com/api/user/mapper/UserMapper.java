package com.api.user.mapper;

import com.api.user.dto.ClaimResponse;
import com.api.user.dto.RoleResponse;
import com.api.user.dto.UserResponse;
import com.api.user.model.Claim;
import com.api.user.model.Role;
import com.api.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role")
    @Mapping(target = "claims", source = "claims")
    @Mapping(target = "passwordGenerated", ignore = true)
    UserResponse toResponse(User user);

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "description", source = "description")
    ClaimResponse toClaimResponse(Claim claim);
}
