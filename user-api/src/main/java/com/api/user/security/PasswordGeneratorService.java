package com.api.user.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Gera senhas seguras usando SecureRandom (CSPRNG) — nunca Random.
 * A senha gerada contém letras maiúsculas, minúsculas, números e símbolos.
 */
@Component
public class PasswordGeneratorService {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String SYMBOLS   = "@$!%*?&_#";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SYMBOLS;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.password.length:12}")
    private int passwordLength;

    /**
     * Gera uma senha aleatória garantindo ao menos 1 char de cada categoria.
     */
    public String generate() {
        if (passwordLength < 8) {
            throw new IllegalStateException("Comprimento mínimo da senha é 8");
        }

        char[] password = new char[passwordLength];
        // Garante pelo menos um char de cada tipo (mitiga senhas fracas)
        password[0] = randomChar(UPPERCASE);
        password[1] = randomChar(LOWERCASE);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SYMBOLS);

        for (int i = 4; i < passwordLength; i++) {
            password[i] = randomChar(ALL_CHARS);
        }

        // Shuffle para não ter padrão previsível
        for (int i = passwordLength - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char tmp = password[i];
            password[i] = password[j];
            password[j] = tmp;
        }

        return new String(password);
    }

    private char randomChar(String chars) {
        return chars.charAt(SECURE_RANDOM.nextInt(chars.length()));
    }
}
