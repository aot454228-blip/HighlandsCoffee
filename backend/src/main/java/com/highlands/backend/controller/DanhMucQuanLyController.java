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

import com.highlands.backend.dto.DanhMucQuanLyDto;
import com.highlands.backend.service.DanhMucQuanLyService;

@RestController
@RequestMapping("/api/quan-ly/danh-muc")
public class DanhMucQuanLyController {

    private final DanhMucQuanLyService service;

    public DanhMucQuanLyController(DanhMucQuanLyService service) {
        this.service = service;
    }

    // GET /api/quan-ly/danh-muc  hoặc  ?trangThai=DANG_BAN
    @GetMapping
    public List<DanhMucQuanLyDto> danhSach(@RequestParam(name = "trangThai", required = false) String trangThai) {
        return service.layDanhSach(trangThai);
    }

    @GetMapping("/{id}")
    public DanhMucQuanLyDto chiTiet(@PathVariable("id") Long id) {
        return service.layTheoId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DanhMucQuanLyDto them(@RequestBody DanhMucQuanLyDto yeuCau) {
        return service.them(yeuCau);
    }

    @PutMapping("/{id}")
    public DanhMucQuanLyDto sua(@PathVariable("id") Long id, @RequestBody DanhMucQuanLyDto yeuCau) {
        return service.sua(id, yeuCau);
    }

    // "Xóa" = ngừng bán
    @DeleteMapping("/{id}")
    public DanhMucQuanLyDto ngungBan(@PathVariable("id") Long id) {
        return service.ngungBan(id);
    }
}