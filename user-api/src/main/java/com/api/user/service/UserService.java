package com.api.user.service;

import com.api.user.dto.CreateUserRequest;
import com.api.user.dto.UserResponse;
import com.api.user.exception.EmailAlreadyExistsException;
import com.api.user.exception.ResourceNotFoundException;
import com.api.user.mapper.UserMapper;
import com.api.user.model.Claim;
import com.api.user.model.Role;
import com.api.user.model.User;
import com.api.user.repository.ClaimRepository;
import com.api.user.repository.RoleRepository;
import com.api.user.repository.UserRepository;
import com.api.user.security.PasswordGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository      userRepository;
    private final RoleRepository      roleRepository;
    private final ClaimRepository     claimRepository;
    private final PasswordEncoder     passwordEncoder;
    private final PasswordGeneratorService passwordGeneratorService;
    private final UserMapper          userMapper;

    /**
     * Cria um novo usuário.
     * - Senha é opcional: gerada automaticamente via SecureRandom se não informada.
     * - Senha é sempre armazenada em hash BCrypt (nunca em texto puro).
     * - E-mail é verificado para unicidade (case-insensitive).
     * - role_id é validado contra a tabela roles.
     * - claims opcionais; somente claims ativas são aceitas.
     *
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Verifica duplicidade de e-mail
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // 2. Valida role
        Role role = roleRepository.findById(request.roleId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Role não encontrado com id: " + request.roleId()));

        // 3. Resolve senha
        boolean passwordGenerated = !StringUtils.hasText(request.password());
        String rawPassword = passwordGenerated
            ? passwordGeneratorService.generate()
            : request.password();

        // 4. Hash BCrypt — nunca persiste senha em texto puro
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 5. Resolve claims (apenas ativas)
        Set<Claim> claims = resolveClaims(request.claimIds());

        // 6. Monta entidade
        User user = new User();
        user.setName(sanitize(request.name()));
        user.setEmail(request.email().toLowerCase().trim());
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setClaims(claims);
        user.setCreatedAt(LocalDate.now());

        User saved = userRepository.save(user);
        log.info("Usuário criado com id={}, email={}", saved.getId(), maskEmail(saved.getEmail()));

        // 7. Monta response sem expor senha
        UserResponse response = userMapper.toResponse(saved);

        // 8. Retorna indicando se senha foi gerada automaticamente
        // Nota: a senha gerada NÃO é retornada na API — deve ser enviada por canal seguro (e-mail)
        return new UserResponse(
            response.id(),
            response.name(),
            response.email(),
            response.role(),
            response.claims(),
            response.createdAt(),
            passwordGenerated
        );
    }

    private Set<Claim> resolveClaims(Set<Long> claimIds) {
        if (claimIds == null || claimIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Claim> found = claimRepository.findAllByIdInAndActiveTrue(claimIds);
        if (found.size() != claimIds.size()) {
            log.warn("Alguns claimIds não foram encontrados ou estão inativos: {}", claimIds);
            // Política: rejeita se qualquer claim inválida for informada
            throw new ResourceNotFoundException(
                "Um ou mais claims informados não existem ou estão inativos");
        }
        return found;
    }

    /**
     * Remove caracteres de controle para prevenir Log Injection e XSS em logs.
     */
    private String sanitize(String value) {
        if (value == null) return null;
        return value.replaceAll("[\\p{Cntrl}]", "").trim();
    }

    /**
     * Mascara e-mail nos logs para proteção de dados (LGPD/GDPR).
     */
    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***@***";
        return email.charAt(0) + "***" + email.substring(at);
    }
}
