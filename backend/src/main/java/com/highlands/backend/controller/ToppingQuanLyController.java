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

import com.highlands.backend.dto.ToppingQuanLyDto;
import com.highlands.backend.service.ToppingQuanLyService;

@RestController
@RequestMapping("/api/quan-ly/topping")
public class ToppingQuanLyController {

    private final ToppingQuanLyService service;

    public ToppingQuanLyController(ToppingQuanLyService service) {
        this.service = service;
    }

    @GetMapping
    public List<ToppingQuanLyDto> danhSach(@RequestParam(name = "trangThai", required = false) String trangThai) {
        return service.layDanhSach(trangThai);
    }

    @GetMapping("/{id}")
    public ToppingQuanLyDto chiTiet(@PathVariable("id") Long id) {
        return service.layTheoId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ToppingQuanLyDto them(@RequestBody ToppingQuanLyDto yeuCau) {
        return service.them(yeuCau);
    }

    @PutMapping("/{id}")
    public ToppingQuanLyDto sua(@PathVariable("id") Long id, @RequestBody ToppingQuanLyDto yeuCau) {
        return service.sua(id, yeuCau);
    }

    // "Xóa" = ngừng bán
    @DeleteMapping("/{id}")
    public ToppingQuanLyDto ngungBan(@PathVariable("id") Long id) {
        return service.ngungBan(id);
    }
}