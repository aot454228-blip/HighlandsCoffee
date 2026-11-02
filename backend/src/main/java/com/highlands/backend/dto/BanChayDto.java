package com.highlands.backend.dto;

/** Một dòng trong bảng sản phẩm bán chạy. */
public record BanChayDto(Long id, String ten, long soLuong, long doanhThu) {
}