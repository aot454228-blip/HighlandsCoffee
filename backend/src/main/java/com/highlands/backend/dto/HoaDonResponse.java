package com.highlands.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Hóa đơn đầy đủ để Swing hiển thị hoặc in. */
public record HoaDonResponse(
        Long id,
        String maHoaDon,
        String maDonHang,
        LocalDateTime thoiGianLap,
        String trangThai,
        String phuongThuc,
        BigDecimal tongTienHang,
        BigDecimal soTienPhaiTra,
        BigDecimal soTienKhachDua,
        BigDecimal tienThua,
        List<DongHoaDonDto> cacDong) {
}