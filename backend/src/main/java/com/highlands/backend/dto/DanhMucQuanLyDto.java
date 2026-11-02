package com.highlands.backend.dto;

/** Danh mục: dùng cho cả dữ liệu gửi lên và trả về (id bị bỏ qua khi thêm mới). */
public record DanhMucQuanLyDto(
        Long id,
        String tenDanhMuc,
        String moTa,
        String trangThai) {
}