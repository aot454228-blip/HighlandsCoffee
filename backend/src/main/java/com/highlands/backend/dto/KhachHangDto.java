package com.highlands.backend.dto;

import java.time.LocalDate;

/** Thông tin khách hàng trả về cho màn hình tra cứu. */
public record KhachHangDto(
        Long id,
        String maKhachHang,
        String hoTen,
        String soDienThoai,
        String email,
        LocalDate ngaySinh,
        String gioiTinh,
        String diaChi,
        int diemTichLuy) {
}
