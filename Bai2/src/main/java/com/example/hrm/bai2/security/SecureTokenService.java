package com.example.hrm.bai2.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class SecureTokenService {

    // ĐÃ SỬA: Thay đổi thời gian sống sang đơn vị PHÚT và đặt bằng 15 phút theo nghiệp vụ
    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;


    private Key getSigningKey() {
        String envSecretKey = System.getenv("JWT_SECRET_KEY");

        if (envSecretKey == null || envSecretKey.trim().isEmpty()) {
            System.err.println("[CRITICAL WARNING] Biến môi trường 'JWT_SECRET_KEY' chưa được cấu hình!");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }

        byte[] keyBytes = envSecretKey.getBytes();
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("Độ dài JWT_SECRET_KEY phải đạt tối thiểu 32 bytes (256 bits)!");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ACCESS_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateCustomExpiredToken(String username, long expirationInSeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationInSeconds, ChronoUnit.SECONDS);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("[VALIDATION FAILED] Token đã hết hạn sử dụng! -> " + e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.out.println("[SECURITY ALERT] Chữ ký Token không hợp lệ! Dấu hiệu giả mạo! -> " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("[VALIDATION FAILED] Token không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        SecureTokenService service = new SecureTokenService();
        String username = "investor_99";

        System.out.println("=== KỊCH BẢN 1: TẠO VÀ XÁC THỰC TOKEN HỢP LỆ ===");
        String validToken = service.generateAccessToken(username);
        System.out.println("Token được cấp thành công: " + validToken);
        System.out.println("Kiểm tra tính hợp lệ ban đầu: " + service.validateToken(validToken));

        System.out.println("\n=== KỊCH BẢN 2: MÔ PHỎNG KẺ TẤN CÔNG GIẢ MẠO TOKEN ===");
        String fakeSecretKey = "FakeKeyForAttackerAttemptingToHackSystem";
        Key attackerKey = Keys.hmacShaKeyFor(fakeSecretKey.getBytes());

        String forgedToken = Jwts.builder()
                .setSubject("admin")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .signWith(attackerKey, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("Token giả mạo do kẻ tấn công ký: " + forgedToken);
        boolean isForgedValid = service.validateToken(forgedToken);
        System.out.println("Kết quả xác thực Token giả mạo: " + isForgedValid + " (Hệ thống từ chối thành công)");

        System.out.println("\n=== KỊCH BẢN 3: CHỨNG MINH TOKEN HẾT HẠN SỬ DỤNG ===");
        System.out.println("Khởi tạo một Token đặc biệt có thời hạn ngắn là 2 giây...");
        String shortLivedToken = service.generateCustomExpiredToken(username, 2);

        System.out.println("Xác thực ngay lập tức: " + service.validateToken(shortLivedToken) + " (Hợp lệ)");

        System.out.println("Luồng hệ thống tạm dừng (Sleep) 3 giây chờ Token hết hạn...");
        TimeUnit.SECONDS.sleep(3);

        System.out.println("Xác thực lại sau khi qua thời gian hết hạn:");
        boolean isExpiredValid = service.validateToken(shortLivedToken);
        System.out.println("Kết quả xác thực Token quá hạn: " + isExpiredValid + " (Hệ thống từ chối thành công)");
    }
}