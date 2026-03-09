package com.api.user.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.Set;

/**
 * DTO de entrada para criação de usuário.
 * password é opcional — se não informado, será gerado automaticamente.
 */
@Builder
public record CreateUserRequest(

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 150, message = "Nome deve ter entre 2 e 150 caracteres")
    String name,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
    String email,

    @NotNull(message = "Papel (role) é obrigatório")
    @Positive(message = "ID do papel deve ser positivo")
    Integer roleId,

    // Senha opcional — sem @NotBlank intencionalmente
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
    String password,

    // Claims opcionais
    Set<@Positive(message = "ID de claim deve ser positivo") Long> claimIds
) {}
