package com.api.user.dto;

import java.time.LocalDate;
import java.util.Set;

/**
 * DTO de saída — NUNCA expõe a senha.
 */
public record UserResponse(
    Long id,
    String name,
    String email,
    RoleResponse role,
    Set<ClaimResponse> claims,
    LocalDate createdAt,
    boolean passwordGenerated
) {}
