package com.highlands.frontend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Một dòng trong danh sách đơn hàng. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DonHangTomTatDto(
        Long id,
        String maDonHang,
        String trangThai,
        String hinhThucPhucVu,
        BigDecimal tongThanhToan,
        String thoiGianDat) {
}
