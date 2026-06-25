package com.example.hrm.bai3.service;

import com.example.hrm.bai3.entity.RefreshToken;
import com.example.hrm.bai3.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long REFRESH_TOKEN_DURATION_MS = 30L * 24 * 60 * 60 * 1000; // 30 ngày

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken createRefreshToken(Long userId, String deviceId) {
        // Nếu client không gửi deviceId, sinh ngẫu nhiên để tránh lỗi Null Constraint
        String finalDeviceId = (deviceId == null || deviceId.trim().isEmpty())
                ? UUID.randomUUID().toString()
                : deviceId;
        refreshTokenRepository.deleteByUserIdAndDeviceId(userId, finalDeviceId);

        String tokenString = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS);

        RefreshToken refreshToken = new RefreshToken(tokenString, userId, finalDeviceId, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }

    public void logoutCurrentDevice(Long userId, String deviceId) {
        if (deviceId != null && !deviceId.trim().isEmpty()) {
            refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
        }
    }

    public void logoutAllDevices(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }


    public void purgeExpiredTokens() {
        Instant now = Instant.now();
        // Thực thi câu lệnh xóa và trả về số lượng bản ghi đã được dọn dẹp thành công
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(now);
        System.out.println("[CRON JOB LOG] Đã dọn dẹp thành công " + deletedCount + " Refresh Token đã hết hạn khỏi hệ thống.");
    }
}