package com.highlands.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.KhachHangDto;
import com.highlands.backend.service.KhachHangService;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangController {

    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    // Ví dụ: GET /api/khach-hang?tuKhoa=lan  (bỏ tuKhoa để lấy 50 khách đầu tiên)
    @GetMapping
    public List<KhachHangDto> timKiem(@RequestParam(name = "tuKhoa", required = false) String tuKhoa) {
        return khachHangService.timKiem(tuKhoa);
    }
}
