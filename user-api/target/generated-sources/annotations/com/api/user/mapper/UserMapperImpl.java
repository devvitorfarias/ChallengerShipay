package com.api.user.mapper;

import com.api.user.dto.ClaimResponse;
import com.api.user.dto.RoleResponse;
import com.api.user.dto.UserResponse;
import com.api.user.model.Claim;
import com.api.user.model.Role;
import com.api.user.model.User;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-09T11:16:45-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.2 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        RoleResponse role = null;
        Set<ClaimResponse> claims = null;
        Long id = null;
        String name = null;
        String email = null;
        LocalDate createdAt = null;

        role = toRoleResponse( user.getRole() );
        claims = claimSetToClaimResponseSet( user.getClaims() );
        id = user.getId();
        name = user.getName();
        email = user.getEmail();
        createdAt = user.getCreatedAt();

        boolean passwordGenerated = false;

        UserResponse userResponse = new UserResponse( id, name, email, role, claims, createdAt, passwordGenerated );

        return userResponse;
    }

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        Integer id = null;
        String description = null;

        id = role.getId();
        description = role.getDescription();

        RoleResponse roleResponse = new RoleResponse( id, description );

        return roleResponse;
    }

    @Override
    public ClaimResponse toClaimResponse(Claim claim) {
        if ( claim == null ) {
            return null;
        }

        String description = null;
        Long id = null;
        Boolean active = null;

        description = claim.getDescription();
        id = claim.getId();
        active = claim.getActive();

        ClaimResponse claimResponse = new ClaimResponse( id, description, active );

        return claimResponse;
    }

    protected Set<ClaimResponse> claimSetToClaimResponseSet(Set<Claim> set) {
        if ( set == null ) {
            return null;
        }

        Set<ClaimResponse> set1 = new LinkedHashSet<ClaimResponse>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Claim claim : set ) {
            set1.add( toClaimResponse( claim ) );
        }

        return set1;
    }
}
