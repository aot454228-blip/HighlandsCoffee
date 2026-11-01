package com.highlands.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.DoiTrangThaiRequest;
import com.highlands.backend.dto.DonHangResponse;
import com.highlands.backend.dto.DonHangTomTatDto;
import com.highlands.backend.exception.DonHangKhongHopLeException;
import com.highlands.backend.service.DonHangTrangThaiService;

@RestController
@RequestMapping("/api/don-hang")
public class DonHangTrangThaiController {

    private final DonHangTrangThaiService trangThaiService;

    public DonHangTrangThaiController(DonHangTrangThaiService trangThaiService) {
        this.trangThaiService = trangThaiService;
    }

    // Ví dụ: GET /api/don-hang?trangThai=MOI_TAO  (bỏ trangThai để lấy 100 đơn mới nhất)
    @GetMapping
    public List<DonHangTomTatDto> layDanhSach(
            @RequestParam(name = "trangThai", required = false) String trangThai) {
        return trangThaiService.layDanhSach(trangThai);
    }

    @PutMapping("/{id}/trang-thai")
    public DonHangResponse doiTrangThai(@PathVariable("id") Long id, @RequestBody DoiTrangThaiRequest request) {
        return trangThaiService.doiTrangThai(id, request);
    }

    @ExceptionHandler(DonHangKhongHopLeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> xuLyLoi(DonHangKhongHopLeException ex) {
        return Map.of("message", ex.getMessage());
    }
}