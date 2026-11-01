package com.highlands.frontend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Kết quả backend trả về sau khi tạo đơn. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DonHangDto(
        Long id,
        String maDonHang,
        String trangThai,
        BigDecimal tongTienHang,
        BigDecimal tongThanhToan,
        String thoiGianDat) {
}
