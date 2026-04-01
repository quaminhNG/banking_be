package com.banking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // secret.getBytes(StandardCharsets.UTF_8)
                                                                            // chuyển secret thành byte array theo chuẩn
                                                                            // UTF-8.

        // Keys.hmacShaKeyFor nhận byte array làm tham số để tạo SecretKey
        // dùng để ký và xác thực chữ ký số của JWT.
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact(); // đóng gói Header.Payload.Signature
        // tạo token và ký bằng getSigningKey(), để đảm bảo tính toàn vẹn
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // check khóa
                .build()
                .parseSignedClaims(token) // check expiration, signature + nội dung = 123, nếu so với token khác thì ném
                                          // lỗi(do ai đó đã thay đổi)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
