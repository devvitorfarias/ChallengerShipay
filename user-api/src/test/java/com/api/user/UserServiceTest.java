package com.api.user.service;

import com.api.user.dto.CreateUserRequest;
import com.api.user.dto.UserResponse;
import com.api.user.exception.EmailAlreadyExistsException;
import com.api.user.exception.ResourceNotFoundException;
import com.api.user.mapper.UserMapper;
import com.api.user.model.Role;
import com.api.user.model.User;
import com.api.user.repository.ClaimRepository;
import com.api.user.repository.RoleRepository;
import com.api.user.repository.UserRepository;
import com.api.user.security.PasswordGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock ClaimRepository claimRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PasswordGeneratorService passwordGeneratorService;
    @Mock UserMapper userMapper;

    @InjectMocks UserService userService;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setDescription("ADMIN");
    }

    @Test
    @DisplayName("Deve criar usuário com senha informada")
    void shouldCreateUserWithProvidedPassword() {
        var request = new CreateUserRequest("João Silva", "joao@example.com", 1, "Senha@123", null);

        when(userRepository.existsByEmailIgnoreCase("joao@example.com")).thenReturn(false);
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("Senha@123")).thenReturn("$2a$12$hash");
        when(claimRepository.findAllByIdInAndActiveTrue(any())).thenReturn(Set.of());

        User savedUser = buildSavedUser(1L, "João Silva", "joao@example.com", role);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse mockResponse = buildUserResponse(1L, "João Silva", "joao@example.com");
        when(userMapper.toResponse(savedUser)).thenReturn(mockResponse);

        UserResponse result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.passwordGenerated()).isFalse();
        verify(passwordGeneratorService, never()).generate();
        verify(passwordEncoder).encode("Senha@123");
    }

    @Test
    @DisplayName("Deve gerar senha automática quando não informada")
    void shouldGeneratePasswordWhenNotProvided() {
        var request = new CreateUserRequest("Maria", "maria@example.com", 1, null, null);

        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(roleRepository.findById(1)).thenReturn(Optional.of(role));
        when(passwordGeneratorService.generate()).thenReturn("Auto@12345");
        when(passwordEncoder.encode("Auto@12345")).thenReturn("$2a$12$autohash");

        User savedUser = buildSavedUser(2L, "Maria", "maria@example.com", role);
        when(userRepository.save(any())).thenReturn(savedUser);

        UserResponse mockResponse = buildUserResponse(2L, "Maria", "maria@example.com");
        when(userMapper.toResponse(savedUser)).thenReturn(mockResponse);

        UserResponse result = userService.createUser(request);

        assertThat(result.passwordGenerated()).isTrue();
        verify(passwordGeneratorService).generate();
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException para e-mail duplicado")
    void shouldThrowWhenEmailAlreadyExists() {
        var request = new CreateUserRequest("Carlos", "carlos@example.com", 1, null, null);
        when(userRepository.existsByEmailIgnoreCase("carlos@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException para role inválida")
    void shouldThrowWhenRoleNotFound() {
        var request = new CreateUserRequest("Ana", "ana@example.com", 99, null, null);
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
        when(roleRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // helpers
    private User buildSavedUser(Long id, String name, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("$2a$12$hash");
        u.setRole(role);
        u.setCreatedAt(LocalDate.now());
        return u;
    }

    private UserResponse buildUserResponse(Long id, String name, String email) {
        return new UserResponse(id, name, email, null, Set.of(), LocalDate.now(), false);
    }
}
