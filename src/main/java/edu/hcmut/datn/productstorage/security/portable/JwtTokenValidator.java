package edu.hcmut.datn.productstorage.security.portable;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtTokenValidator {
    public Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(
                        loadPublicKey())
                .requireIssuer("identity-service")
                .build()
                .parseClaimsJws(token);
    }

    private static PublicKey loadPublicKey() {
        try {
            Resource resource = new ClassPathResource("keys/public.pem");

            String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String publicKeyContent = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyContent);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(keySpec);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key", e);
        }
    }
}
