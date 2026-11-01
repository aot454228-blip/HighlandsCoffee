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
@Table(name = "hoa_don")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_hoa_don", nullable = false, unique = true, length = 30)
    private String maHoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private DonHang donHang;

    // Tài khoản lập hóa đơn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id")
    private TaiKhoan taiKhoan;

    @Column(name = "thoi_gian_lap", nullable = false)
    private LocalDateTime thoiGianLap;

    @Column(name = "so_tien_phai_tra", nullable = false, precision = 14, scale = 0)
    private BigDecimal soTienPhaiTra;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    public Long getId() { return id; }
    public String getMaHoaDon() { return maHoaDon; }
    public DonHang getDonHang() { return donHang; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public LocalDateTime getThoiGianLap() { return thoiGianLap; }
    public BigDecimal getSoTienPhaiTra() { return soTienPhaiTra; }
    public String getTrangThai() { return trangThai; }

    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public void setThoiGianLap(LocalDateTime thoiGianLap) { this.thoiGianLap = thoiGianLap; }
    public void setSoTienPhaiTra(BigDecimal soTienPhaiTra) { this.soTienPhaiTra = soTienPhaiTra; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}