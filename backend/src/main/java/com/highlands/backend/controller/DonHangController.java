package com.highlands.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.DonHangResponse;
import com.highlands.backend.dto.TaoDonHangRequest;
import com.highlands.backend.exception.DonHangKhongHopLeException;
import com.highlands.backend.service.DonHangService;

@RestController
@RequestMapping("/api/don-hang")
public class DonHangController {

    private final DonHangService donHangService;

    public DonHangController(DonHangService donHangService) {
        this.donHangService = donHangService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonHangResponse taoDon(@RequestBody TaoDonHangRequest request) {
        return donHangService.taoDonTaiQuay(request);
    }

    // Dữ liệu sai: trả mã 400 kèm thông báo (cùng dạng {"message": "..."} như API đăng nhập)
    @ExceptionHandler(DonHangKhongHopLeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> xuLyDonKhongHopLe(DonHangKhongHopLeException ex) {
        return Map.of("message", ex.getMessage());
    }
}