package com.highlands.backend.exception;

/** Ném ra khi dữ liệu đặt hàng sai (món không tồn tại, số lượng sai, ...). */
public class DonHangKhongHopLeException extends RuntimeException {

    public DonHangKhongHopLeException(String message) {
        super(message);
    }
}