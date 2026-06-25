package com.example.hrm.bai3.repository;

import com.example.hrm.bai3.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Xóa token của một thiết bị cụ thể thuộc về user
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId AND r.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(Long userId, String deviceId);

    // Kịch bản Logout tất cả thiết bị: Xóa sạch token theo userId
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId")
    void deleteAllByUserId(Long userId);

    // Phục vụ cho cơ chế dọn dẹp tự động
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    int deleteExpiredTokens(Instant now);
}