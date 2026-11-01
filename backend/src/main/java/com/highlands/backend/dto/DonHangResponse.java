package com.highlands.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Kết quả trả về sau khi tạo đơn. */
public record DonHangResponse(
        Long id,
        String maDonHang,
        String trangThai,
        BigDecimal tongTienHang,
        BigDecimal tongThanhToan,
        LocalDateTime thoiGianDat) {
}