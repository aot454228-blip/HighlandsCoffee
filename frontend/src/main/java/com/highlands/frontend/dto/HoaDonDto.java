package com.highlands.frontend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Hóa đơn backend trả về sau khi thanh toán. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HoaDonDto(
        Long id,
        String maHoaDon,
        String maDonHang,
        String thoiGianLap,
        String trangThai,
        String phuongThuc,
        BigDecimal tongTienHang,
        BigDecimal soTienPhaiTra,
        BigDecimal soTienKhachDua,
        BigDecimal tienThua,
        List<DongHoaDonDto> cacDong) {
}
