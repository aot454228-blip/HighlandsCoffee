package com.highlands.backend.dto;

import java.util.List;

/** Sản phẩm kèm bảng giá theo size. Dùng lại KichThuocGiaDto của màn hình bán hàng. */
public record SanPhamQuanLyResponse(
        Long id,
        String maSanPham,
        Long danhMucId,
        String tenDanhMuc,
        String tenSanPham,
        String moTa,
        String hinhAnh,
        String trangThai,
        List<KichThuocGiaDto> kichThuoc) {
}