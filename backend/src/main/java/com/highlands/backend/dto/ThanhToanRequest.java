package com.highlands.backend.dto;

import java.math.BigDecimal;

/**
 * Dữ liệu Swing gửi lên khi thanh toán.
 * Không có số tiền cần trả: backend tự lấy từ đơn hàng.
 * phuongThuc: TIEN_MAT (cần soTienKhachDua) hoặc CHUYEN_KHOAN (cần maGiaoDich).
 */
public record ThanhToanRequest(
        Long taiKhoanId,
        String phuongThuc,
        BigDecimal soTienKhachDua,
        String maGiaoDich) {
}