package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtDecoderConfigTest {

    // Clés jetables de test (jamais la vraie clé du .env)
    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OTHER_SECRET = Base64.getEncoder().encodeToString(filled(32, (byte) 3));

    private final JwtDecoderConfig config = new JwtDecoderConfig();

    private static byte[] filled(int length, byte value) {
        byte[] bytes = new byte[length];
        Arrays.fill(bytes, value);
        return bytes;
    }

    private String tokenSignedWith(String secret) {
        User user = new User();
        user.setLogin("decoder-test-user");
        return new JwtService(secret, 3_600_000L).generateToken(user);
    }

    @Test
    public void test_decoder_accepts_token_generated_by_JwtService() {
        JwtDecoder decoder = config.jwtDecoder(SECRET);

        Jwt jwt = decoder.decode(tokenSignedWith(SECRET));

        assertThat(jwt.getSubject()).isEqualTo("decoder-test-user");
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    public void test_decoder_rejects_token_signed_with_another_key() {
        JwtDecoder decoder = config.jwtDecoder(SECRET);

        assertThatThrownBy(() -> decoder.decode(tokenSignedWith(OTHER_SECRET)))
                .isInstanceOf(JwtException.class);
    }

    @Test
    public void test_decoder_rejects_malformed_token() {
        JwtDecoder decoder = config.jwtDecoder(SECRET);

        assertThatThrownBy(() -> decoder.decode("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    public void test_secret_too_short_throws_IllegalStateException() {
        String shortSecret = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> config.jwtDecoder(shortSecret))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void test_secret_not_base64_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> config.jwtDecoder("%%% not base64 %%%"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
