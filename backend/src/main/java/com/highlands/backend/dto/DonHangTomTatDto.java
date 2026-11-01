package com.highlands.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Một dòng trong danh sách đơn hàng. */
public record DonHangTomTatDto(
        Long id,
        String maDonHang,
        String trangThai,
        String hinhThucPhucVu,
        BigDecimal tongThanhToan,
        LocalDateTime thoiGianDat) {
}