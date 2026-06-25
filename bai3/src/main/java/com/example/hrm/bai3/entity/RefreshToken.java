package com.example.hrm.bai3.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_user_device", columnList = "user_id, device_id")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ĐÃ BỔ SUNG: Định danh thiết bị/phiên làm việc cụ thể
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant expiryDate;

    public RefreshToken() {}

    public RefreshToken(String token, Long userId, String deviceId, Instant expiryDate) {
        this.token = token;
        this.userId = userId;
        this.deviceId = deviceId;
        this.expiryDate = expiryDate;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public Instant getExpiryDate() { return expiryDate; }

    public void setToken(String token) { this.token = token; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}