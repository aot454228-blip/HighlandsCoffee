package com.highlands.backend.dto;

import java.util.List;

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