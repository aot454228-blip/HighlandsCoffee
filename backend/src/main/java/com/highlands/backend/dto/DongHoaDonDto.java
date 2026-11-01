package com.highlands.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/** Một dòng món trên hóa đơn. */
public record DongHoaDonDto(
        String tenSanPham,
        String tenKichThuoc,
        int soLuong,
        BigDecimal donGia,
        List<String> cacTopping,
        BigDecimal thanhTien) {
}