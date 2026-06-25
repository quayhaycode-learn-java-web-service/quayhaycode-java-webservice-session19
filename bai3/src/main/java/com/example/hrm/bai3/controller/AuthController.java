package com.example.hrm.bai3.controller;

import com.example.hrm.bai3.entity.RefreshToken;
import com.example.hrm.bai3.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    public AuthController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest,
                                   @RequestHeader(value = "X-Device-Id", required = false) String deviceIdFromHeader) {
        // Client có thể gửi deviceId qua Body JSON hoặc Custom Header (X-Device-Id)
        String deviceId = loginRequest.getOrDefault("deviceId", deviceIdFromHeader);
        Long fakeUserId = 12345L; // Giả lập userId tìm được sau khi xác thực mật khẩu thành công

        RefreshToken token = refreshTokenService.createRefreshToken(fakeUserId, deviceId);
        return ResponseEntity.ok(Map.of(
                "accessToken", "mock-access-token-xyz",
                "refreshToken", token.getToken(),
                "deviceId", token.getDeviceId()
        ));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> logoutRequest,
                                    @RequestHeader(value = "X-Device-Id", required = false) String deviceIdFromHeader) {
        Long currentUserId = 12345L; // Giả lập lấy userId từ SecurityContext (đã xác thực qua Access Token)
        String deviceId = logoutRequest.getOrDefault("deviceId", deviceIdFromHeader);

        if (deviceId == null || deviceId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu thông tin deviceId để thực hiện đăng xuất cục bộ!"));
        }

        refreshTokenService.logoutCurrentDevice(currentUserId, deviceId);
        return ResponseEntity.ok(Map.of("message", "Đã đăng xuất thành công thiết bị: " + deviceId));
    }


    @PostMapping("/logoutAllDevices")
    public ResponseEntity<?> logoutAllDevices() {
        Long currentUserId = 12345L; // Giả lập lấy userId từ SecurityContext

        refreshTokenService.logoutAllDevices(currentUserId);
        return ResponseEntity.ok(Map.of("message", "Đã vô hiệu hóa toàn bộ phiên đăng nhập trên tất cả các thiết bị thành công!"));
    }
}