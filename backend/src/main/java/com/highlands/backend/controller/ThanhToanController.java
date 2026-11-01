package com.highlands.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.HoaDonResponse;
import com.highlands.backend.dto.ThanhToanRequest;
import com.highlands.backend.exception.DonHangKhongHopLeException;
import com.highlands.backend.service.ThanhToanService;

@RestController
@RequestMapping("/api/don-hang")
public class ThanhToanController {

    private final ThanhToanService thanhToanService;

    public ThanhToanController(ThanhToanService thanhToanService) {
        this.thanhToanService = thanhToanService;
    }

    @PostMapping("/{id}/thanh-toan")
    @ResponseStatus(HttpStatus.CREATED)
    public HoaDonResponse thanhToan(@PathVariable("id") Long id, @RequestBody ThanhToanRequest request) {
        return thanhToanService.thanhToan(id, request);
    }

    @GetMapping("/{id}/hoa-don")
    public HoaDonResponse xemHoaDon(@PathVariable("id") Long id) {
        return thanhToanService.xemHoaDon(id);
    }

    @ExceptionHandler(DonHangKhongHopLeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> xuLyLoi(DonHangKhongHopLeException ex) {
        return Map.of("message", ex.getMessage());
    }
}