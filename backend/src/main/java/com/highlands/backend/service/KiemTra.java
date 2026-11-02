package com.highlands.backend.service;

import java.math.BigDecimal;

import com.highlands.backend.exception.QuanLyException;

/** Các hàm kiểm tra dữ liệu dùng chung cho phần quản lý. */
final class KiemTra {

    // Cột DECIMAL(12,0) chứa tối đa 12 chữ số
    private static final BigDecimal TIEN_TOI_DA = new BigDecimal("999999999999");

    private KiemTra() {
    }

    /** Chuỗi bắt buộc: cắt khoảng trắng hai đầu, không được trống, không vượt độ dài tối đa của cột. */
    static String chuoiBatBuoc(String giaTri, String tenTruong, int toiDa) {
        String s = giaTri == null ? "" : giaTri.trim();
        if (s.isEmpty()) {
            throw QuanLyException.saiDuLieu(tenTruong + " không được để trống");
        }
        if (s.length() > toiDa) {
            throw QuanLyException.saiDuLieu(tenTruong + " tối đa " + toiDa + " ký tự");
        }
        return s;
    }

    /** Chuỗi tùy chọn: để trống thì trả về null. */
    static String chuoiTuyChon(String giaTri, String tenTruong, int toiDa) {
        if (giaTri == null || giaTri.isBlank()) {
            return null;
        }
        String s = giaTri.trim();
        if (s.length() > toiDa) {
            throw QuanLyException.saiDuLieu(tenTruong + " tối đa " + toiDa + " ký tự");
        }
        return s;
    }

    /** Số tiền nguyên (đồng). choPhepBangKhong = true thì cho phép bằng 0. */
    static BigDecimal soTienNguyen(BigDecimal giaTri, String tenTruong, boolean choPhepBangKhong) {
        if (giaTri == null) {
            throw QuanLyException.saiDuLieu(tenTruong + " không được để trống");
        }
        if (giaTri.stripTrailingZeros().scale() > 0) {
            throw QuanLyException.saiDuLieu(tenTruong + " phải là số nguyên (đồng)");
        }
        int dau = giaTri.signum();
        if (dau < 0) {
            throw QuanLyException.saiDuLieu(tenTruong + " không được âm");
        }
        if (dau == 0 && !choPhepBangKhong) {
            throw QuanLyException.saiDuLieu(tenTruong + " phải lớn hơn 0");
        }
        if (giaTri.compareTo(TIEN_TOI_DA) > 0) {
            throw QuanLyException.saiDuLieu(tenTruong + " quá lớn");
        }
        return giaTri.setScale(0);
    }

    /** Trạng thái: để trống thì dùng giá trị mặc định, ngoài danh sách hợp lệ thì báo lỗi. */
    static String trangThai(String giaTri, String macDinh, String... hopLe) {
        if (giaTri == null || giaTri.isBlank()) {
            return macDinh;
        }
        String s = giaTri.trim().toUpperCase();
        for (String h : hopLe) {
            if (h.equals(s)) {
                return s;
            }
        }
        throw QuanLyException.saiDuLieu("Trạng thái không hợp lệ (chỉ nhận: " + String.join(", ", hopLe) + ")");
    }
}