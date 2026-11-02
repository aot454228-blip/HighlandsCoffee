package com.highlands.backend.dto;

import java.math.BigDecimal;

/** Topping: dùng cho cả dữ liệu gửi lên và trả về. */
public record ToppingQuanLyDto(
        Long id,
        String tenTopping,
        BigDecimal giaBan,
        String hinhAnh,
        String trangThai) {
}