package com.highlands.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "thanh_toan")
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    // Tài khoản thu tiền
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id")
    private TaiKhoan taiKhoan;

    @Column(name = "phuong_thuc", nullable = false, length = 20)
    private String phuongThuc;

    @Column(name = "so_tien", nullable = false, precision = 14, scale = 0)
    private BigDecimal soTien;

    // Chỉ dùng cho tiền mặt
    @Column(name = "so_tien_khach_dua", precision = 14, scale = 0)
    private BigDecimal soTienKhachDua;

    @Column(name = "tien_thua", precision = 14, scale = 0)
    private BigDecimal tienThua;

    // Chỉ dùng cho chuyển khoản
    @Column(name = "ma_giao_dich", length = 50)
    private String maGiaoDich;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "thoi_gian_thanh_toan", nullable = false)
    private LocalDateTime thoiGianThanhToan;

    public Long getId() { return id; }
    public HoaDon getHoaDon() { return hoaDon; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public String getPhuongThuc() { return phuongThuc; }
    public BigDecimal getSoTien() { return soTien; }
    public BigDecimal getSoTienKhachDua() { return soTienKhachDua; }
    public BigDecimal getTienThua() { return tienThua; }
    public String getMaGiaoDich() { return maGiaoDich; }
    public String getTrangThai() { return trangThai; }
    public LocalDateTime getThoiGianThanhToan() { return thoiGianThanhToan; }

    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public void setPhuongThuc(String phuongThuc) { this.phuongThuc = phuongThuc; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public void setSoTienKhachDua(BigDecimal soTienKhachDua) { this.soTienKhachDua = soTienKhachDua; }
    public void setTienThua(BigDecimal tienThua) { this.tienThua = tienThua; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) { this.thoiGianThanhToan = thoiGianThanhToan; }
}