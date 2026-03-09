package com.api.user.repository;

import com.api.user.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Set<Claim> findAllByIdInAndActiveTrue(Set<Long> ids);
}
