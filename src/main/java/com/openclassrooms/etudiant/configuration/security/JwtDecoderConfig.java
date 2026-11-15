package com.openclassrooms.etudiant.configuration.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Décodeur de JWT symétrique (HS256), utilisant la même clé que {@link com.openclassrooms.etudiant.service.JwtService}.
 * Il vérifie la signature et la date d'expiration des tokens reçus dans l'en-tête Authorization: Bearer.
 */
@Configuration
public class JwtDecoderConfig {

    private static final int MIN_KEY_BYTES = 32; // 256 bits requis pour HS256

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("jwt.secret must be at least 256 bits (Base64-encoded)");
        }
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
