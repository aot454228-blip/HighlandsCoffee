package com.highlands.frontend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Một dòng món trên hóa đơn. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DongHoaDonDto(
        String tenSanPham,
        String tenKichThuoc,
        int soLuong,
        BigDecimal donGia,
        List<String> cacTopping,
        BigDecimal thanhTien) {
}
