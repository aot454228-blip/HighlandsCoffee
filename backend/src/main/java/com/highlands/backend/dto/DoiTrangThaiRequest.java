package com.highlands.backend.dto;

/** Dữ liệu Swing gửi lên khi đổi trạng thái đơn. lyDo chỉ bắt buộc khi hủy (DA_HUY). */
public record DoiTrangThaiRequest(
        Long taiKhoanId,
        String trangThaiMoi,
        String lyDo) {
}