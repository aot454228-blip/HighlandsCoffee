package com.highlands.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Bảng khuyen_mai. Hai cột created_at, updated_at do MySQL tự điền nên không khai báo ở đây. */
@Entity
@Table(name = "khuyen_mai")
public class KhuyenMai {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_khuyen_mai", nullable = false, length = 150)
    private String tenKhuyenMai;

    // Trống = khuyến mãi tự áp dụng, không cần nhập mã
    @Column(name = "ma_code", length = 30)
    private String maCode;

    // PHAN_TRAM hoặc SO_TIEN
    @Column(name = "loai_giam", nullable = false, length = 20)
    private String loaiGiam;

    @Column(name = "gia_tri_giam", nullable = false, precision = 12, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "giam_toi_da", precision = 12, scale = 0)
    private BigDecimal giamToiDa;

    @Column(name = "don_toi_thieu", nullable = false, precision = 12, scale = 0)
    private BigDecimal donToiThieu = BigDecimal.ZERO;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "so_luot_toi_da")
    private Integer soLuotToiDa;

    @Column(name = "so_luot_da_dung", nullable = false)
    private int soLuotDaDung = 0;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    // HOAT_DONG hoặc TAM_DUNG
    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    public Long getId() { return id; }
    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public String getMaCode() { return maCode; }
    public String getLoaiGiam() { return loaiGiam; }
    public BigDecimal getGiaTriGiam() { return giaTriGiam; }
    public BigDecimal getGiamToiDa() { return giamToiDa; }
    public BigDecimal getDonToiThieu() { return donToiThieu; }
    public LocalDateTime getNgayBatDau() { return ngayBatDau; }
    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public Integer getSoLuotToiDa() { return soLuotToiDa; }
    public int getSoLuotDaDung() { return soLuotDaDung; }
    public String getHinhAnh() { return hinhAnh; }
    public String getTrangThai() { return trangThai; }

    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }
    public void setMaCode(String maCode) { this.maCode = maCode; }
    public void setLoaiGiam(String loaiGiam) { this.loaiGiam = loaiGiam; }
    public void setGiaTriGiam(BigDecimal giaTriGiam) { this.giaTriGiam = giaTriGiam; }
    public void setGiamToiDa(BigDecimal giamToiDa) { this.giamToiDa = giamToiDa; }
    public void setDonToiThieu(BigDecimal donToiThieu) { this.donToiThieu = donToiThieu; }
    public void setNgayBatDau(LocalDateTime ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public void setSoLuotToiDa(Integer soLuotToiDa) { this.soLuotToiDa = soLuotToiDa; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}