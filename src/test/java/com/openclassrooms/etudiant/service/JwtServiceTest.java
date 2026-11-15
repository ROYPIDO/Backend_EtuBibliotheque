package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtServiceTest {

    private static final long EXPIRATION_MS = 3600000;
    private static final String LOGIN = "login";

    private byte[] keyBytes;
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        jwtService = new JwtService(Base64.getEncoder().encodeToString(keyBytes), EXPIRATION_MS);
        user = new User();
        user.setLogin(LOGIN);
    }

    private NimbusJwtDecoder decoder(byte[] bytes) {
        SecretKey key = new SecretKeySpec(bytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Test
    public void test_generate_token_is_signed_and_contains_subject() {
        // WHEN
        String token = jwtService.generateToken(user);

        // THEN
        assertThat(token).isNotBlank();
        Jwt jwt = decoder(keyBytes).decode(token);
        assertThat(jwt.getSubject()).isEqualTo(LOGIN);
    }

    @Test
    public void test_generate_token_expiration_matches_configuration() {
        // WHEN
        Jwt jwt = decoder(keyBytes).decode(jwtService.generateToken(user));

        // THEN
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toMillis())
                .isEqualTo(EXPIRATION_MS);
    }

    @Test
    public void test_token_rejected_with_another_key() {
        // GIVEN
        String token = jwtService.generateToken(user);
        byte[] otherKey = new byte[32];
        new SecureRandom().nextBytes(otherKey);

        // THEN
        assertThatThrownBy(() -> decoder(otherKey).decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    public void test_secret_too_short_throws_IllegalStateException() {
        // GIVEN
        String shortSecret = Base64.getEncoder().encodeToString(new byte[16]);

        // THEN
        assertThatThrownBy(() -> new JwtService(shortSecret, EXPIRATION_MS))
                .isInstanceOf(IllegalStateException.class);
    }
}
