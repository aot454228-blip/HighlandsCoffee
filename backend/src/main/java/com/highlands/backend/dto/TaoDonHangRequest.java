package com.highlands.backend.dto;

import java.util.List;

/** Dữ liệu Swing gửi lên khi bấm tạo đơn tại quầy. */
public record TaoDonHangRequest(
        Long taiKhoanId,
        String hinhThucPhucVu,
        String ghiChu,
        List<MonDatRequest> cacMon) {
}