package com.highlands.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.LoginRequest;
import com.highlands.backend.dto.LoginResponse;
import com.highlands.backend.exception.DangNhapThatBaiException;
import com.highlands.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse dangNhap(@RequestBody LoginRequest request) {
        return authService.dangNhap(request);
    }

    // Khi đăng nhập thất bại: trả mã 401 kèm thông báo để Swing hiển thị
    @ExceptionHandler(DangNhapThatBaiException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> xuLyDangNhapThatBai(DangNhapThatBaiException ex) {
        return Map.of("message", ex.getMessage());
    }
}