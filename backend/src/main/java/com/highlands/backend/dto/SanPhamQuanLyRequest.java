package com.highlands.backend.dto;

import java.util.List;

/**
 * Dữ liệu thêm/sửa sản phẩm. maSanPham để trống thì backend tự sinh (SP008, SP009...);
 * khi sửa, maSanPham bị bỏ qua (mã không đổi được). cacGia phải có ít nhất một size.
 */
public record SanPhamQuanLyRequest(
        String maSanPham,
        Long danhMucId,
        String tenSanPham,
        String moTa,
        String hinhAnh,
        String trangThai,
        List<GiaSizeRequest> cacGia) {
}