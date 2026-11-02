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

import com.highlands.backend.dto.KichThuocDto;
import com.highlands.backend.dto.SanPhamQuanLyRequest;
import com.highlands.backend.dto.SanPhamQuanLyResponse;
import com.highlands.backend.service.SanPhamQuanLyService;

@RestController
@RequestMapping("/api/quan-ly")
public class SanPhamQuanLyController {

    private final SanPhamQuanLyService service;

    public SanPhamQuanLyController(SanPhamQuanLyService service) {
        this.service = service;
    }

    // GET /api/quan-ly/san-pham  hoặc  ?q=phin  ?danhMucId=1  ?trangThai=NGUNG_BAN  (kết hợp được)
    @GetMapping("/san-pham")
    public List<SanPhamQuanLyResponse> danhSach(
            @RequestParam(name = "q", required = false) String tuKhoa,
            @RequestParam(name = "danhMucId", required = false) Long danhMucId,
            @RequestParam(name = "trangThai", required = false) String trangThai) {
        return service.layDanhSach(tuKhoa, danhMucId, trangThai);
    }

    @GetMapping("/san-pham/{id}")
    public SanPhamQuanLyResponse chiTiet(@PathVariable("id") Long id) {
        return service.layTheoId(id);
    }

    @PostMapping("/san-pham")
    @ResponseStatus(HttpStatus.CREATED)
    public SanPhamQuanLyResponse them(@RequestBody SanPhamQuanLyRequest yeuCau) {
        return service.them(yeuCau);
    }

    @PutMapping("/san-pham/{id}")
    public SanPhamQuanLyResponse sua(@PathVariable("id") Long id, @RequestBody SanPhamQuanLyRequest yeuCau) {
        return service.sua(id, yeuCau);
    }

    // "Xóa" = ngừng bán
    @DeleteMapping("/san-pham/{id}")
    public SanPhamQuanLyResponse ngungBan(@PathVariable("id") Long id) {
        return service.ngungBan(id);
    }

    // Danh sách size để form sản phẩm chọn
    @GetMapping("/kich-thuoc")
    public List<KichThuocDto> kichThuoc() {
        return service.layKichThuoc();
    }
}