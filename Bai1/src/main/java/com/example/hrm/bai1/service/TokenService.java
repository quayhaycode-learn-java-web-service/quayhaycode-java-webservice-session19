package com.example.hrm.bai1.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TokenService {
    private final String SECRET_KEY = "superSecretKey";

    // ĐÃ SỬA: Thay đổi cấu hình từ 30 giây thành 15 phút theo đúng yêu cầu nghiệp vụ
    private final long ACCESS_TOKEN_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(15);

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_MS);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Hàm bổ trợ dùng để lấy Claims phục vụ cho việc test/chứng minh
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // Main method chứng minh thời gian hết hạn của Token
    public static void main(String[] args) {
        TokenService tokenService = new TokenService();
        String username = "testUser";

        // 1. Tạo token
        String token = tokenService.generateAccessToken(username);
        System.out.println("=== THỬ NGHIỆM TẠO VÀ KIỂM TRA ĐỘ DÀI THỜI GIAN JWT ===");
        System.out.println("Token được tạo thành công: " + token);

        // 2. Lấy thông tin thời gian từ Token để chứng minh
        Claims claims = tokenService.getClaimsFromToken(token);
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        System.out.println("\n--- Kết quả phân tích thời gian ---");
        System.out.println("Thời điểm phát hành (Issued At): " + issuedAt);
        System.out.println("Thời điểm hết hạn (Expiration) : " + expiration);

        // 3. Tính toán khoảng chênh lệch
        long durationMs = expiration.getTime() - issuedAt.getTime();
        long durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);

        System.out.println("Khoảng thời gian sống của token (mili giây): " + durationMs + " ms");
        System.out.println("Khoảng thời gian sống của token (phút): " + durationMinutes + " phút");

        // 4. Assert kiểm tra tính đúng đắn
        if (durationMinutes == 15) {
            System.out.println("\n KẾT LUẬN: Code chạy ĐÚNG. Access Token có thời hạn chính xác là 15 phút.");
        } else {
            System.out.println("\n KẾT LUẬN: Code chạy SAI yêu cầu nghiệp vụ.");
        }
    }
}