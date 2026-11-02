package com.highlands.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Khuyến mãi: dùng cho cả dữ liệu gửi lên và trả về.
 * Ngày giờ có dạng 2026-11-30T23:59:59. soLuotDaDung chỉ để đọc, gửi lên sẽ bị bỏ qua.
 */
public record KhuyenMaiQuanLyDto(
        Long id,
        String tenKhuyenMai,
        String maCode,
        String loaiGiam,
        BigDecimal giaTriGiam,
        BigDecimal giamToiDa,
        BigDecimal donToiThieu,
        LocalDateTime ngayBatDau,
        LocalDateTime ngayKetThuc,
        Integer soLuotToiDa,
        Integer soLuotDaDung,
        String hinhAnh,
        String trangThai) {
}