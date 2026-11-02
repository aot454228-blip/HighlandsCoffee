package com.highlands.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.KhuyenMaiQuanLyDto;
import com.highlands.backend.service.KhuyenMaiQuanLyService;

@RestController
@RequestMapping("/api/quan-ly/khuyen-mai")
public class KhuyenMaiQuanLyController {

    private final KhuyenMaiQuanLyService service;

    public KhuyenMaiQuanLyController(KhuyenMaiQuanLyService service) {
        this.service = service;
    }

    // GET /api/quan-ly/khuyen-mai  hoặc  ?q=giam  ?trangThai=HOAT_DONG
    @GetMapping
    public List<KhuyenMaiQuanLyDto> danhSach(
            @RequestParam(name = "q", required = false) String tuKhoa,
            @RequestParam(name = "trangThai", required = false) String trangThai) {
        return service.layDanhSach(tuKhoa, trangThai);
    }

    @GetMapping("/{id}")
    public KhuyenMaiQuanLyDto chiTiet(@PathVariable("id") Long id) {
        return service.layTheoId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KhuyenMaiQuanLyDto them(@RequestBody KhuyenMaiQuanLyDto yeuCau) {
        return service.them(yeuCau);
    }

    @PutMapping("/{id}")
    public KhuyenMaiQuanLyDto sua(@PathVariable("id") Long id, @RequestBody KhuyenMaiQuanLyDto yeuCau) {
        return service.sua(id, yeuCau);
    }

    // "Xóa" = tạm dừng
    @DeleteMapping("/{id}")
    public KhuyenMaiQuanLyDto tamDung(@PathVariable("id") Long id) {
        return service.tamDung(id);
    }
}