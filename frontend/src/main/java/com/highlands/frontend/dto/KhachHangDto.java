package com.highlands.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Thông tin một khách hàng. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KhachHangDto(
        Long id,
        String maKhachHang,
        String hoTen,
        String soDienThoai,
        String email,
        String ngaySinh,
        String gioiTinh,
        String diaChi,
        int diemTichLuy) {
}
