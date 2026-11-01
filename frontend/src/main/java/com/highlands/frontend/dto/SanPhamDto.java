package com.highlands.frontend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SanPhamDto(
        Long id,
        String maSanPham,
        String tenSanPham,
        Long danhMucId,
        String tenDanhMuc,
        String moTa,
        String hinhAnh,
        List<KichThuocGiaDto> kichThuoc) {
}