package com.highlands.backend.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chi_tiet_don_hang")
public class ChiTietDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private DonHang donHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kich_thuoc_id", nullable = false)
    private KichThuoc kichThuoc;

    // "Chụp" lại tên và giá tại lúc đặt, để sau này đổi giá menu không làm sai đơn cũ
    @Column(name = "ten_san_pham", nullable = false, length = 150)
    private String tenSanPham;

    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    @Column(name = "so_luong", nullable = false)
    private int soLuong;

    @Column(name = "muc_duong", length = 20)
    private String mucDuong;

    @Column(name = "muc_da", length = 20)
    private String mucDa;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "thanh_tien", nullable = false, precision = 14, scale = 0)
    private BigDecimal thanhTien;

    @OneToMany(mappedBy = "chiTietDonHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDonHangTopping> cacTopping = new ArrayList<>();

    public void themTopping(ChiTietDonHangTopping topping) {
        cacTopping.add(topping);
        topping.setChiTietDonHang(this);
    }

    public Long getId() { return id; }
    public DonHang getDonHang() { return donHang; }
    public SanPham getSanPham() { return sanPham; }
    public KichThuoc getKichThuoc() { return kichThuoc; }
    public String getTenSanPham() { return tenSanPham; }
    public BigDecimal getDonGia() { return donGia; }
    public int getSoLuong() { return soLuong; }
    public String getMucDuong() { return mucDuong; }
    public String getMucDa() { return mucDa; }
    public String getGhiChu() { return ghiChu; }
    public BigDecimal getThanhTien() { return thanhTien; }
    public List<ChiTietDonHangTopping> getCacTopping() { return cacTopping; }

    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }
    public void setKichThuoc(KichThuoc kichThuoc) { this.kichThuoc = kichThuoc; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public void setMucDuong(String mucDuong) { this.mucDuong = mucDuong; }
    public void setMucDa(String mucDa) { this.mucDa = mucDa; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
}