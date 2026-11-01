package com.highlands.backend.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.highlands.backend.dto.LoginRequest;
import com.highlands.backend.dto.LoginResponse;
import com.highlands.backend.entity.TaiKhoan;
import com.highlands.backend.exception.DangNhapThatBaiException;
import com.highlands.backend.repository.TaiKhoanRepository;

@Service
public class AuthService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(TaiKhoanRepository taiKhoanRepository, PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse dangNhap(LoginRequest request) {
        if (request.tenDangNhap() == null || request.tenDangNhap().isBlank()
                || request.matKhau() == null || request.matKhau().isEmpty()) {
            throw new DangNhapThatBaiException("Vui lòng nhập tên đăng nhập và mật khẩu");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(request.tenDangNhap().trim())
                .orElseThrow(() -> new DangNhapThatBaiException("Sai tên đăng nhập hoặc mật khẩu"));

        // So sánh mật khẩu người dùng gõ với chuỗi BCrypt trong database
        if (!passwordEncoder.matches(request.matKhau(), taiKhoan.getMatKhauMaHoa())) {
            throw new DangNhapThatBaiException("Sai tên đăng nhập hoặc mật khẩu");
        }

        if ("BI_KHOA".equals(taiKhoan.getTrangThai())) {
            throw new DangNhapThatBaiException("Tài khoản đã bị khóa");
        }

        taiKhoan.setLanDangNhapCuoi(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        return new LoginResponse(taiKhoan.getId(), taiKhoan.getTenDangNhap(), taiKhoan.getVaiTro());
    }
}