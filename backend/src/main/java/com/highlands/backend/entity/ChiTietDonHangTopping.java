package com.highlands.backend.entity;

import java.math.BigDecimal;

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
@Table(name = "chi_tiet_don_hang_topping")
public class ChiTietDonHangTopping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_tiet_don_hang_id", nullable = false)
    private ChiTietDonHang chiTietDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topping_id", nullable = false)
    private Topping topping;

    @Column(name = "so_luong", nullable = false)
    private int soLuong = 1;

    // Giá topping tại lúc đặt
    @Column(name = "don_gia", nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    public Long getId() { return id; }
    public ChiTietDonHang getChiTietDonHang() { return chiTietDonHang; }
    public Topping getTopping() { return topping; }
    public int getSoLuong() { return soLuong; }
    public BigDecimal getDonGia() { return donGia; }

    public void setChiTietDonHang(ChiTietDonHang chiTietDonHang) { this.chiTietDonHang = chiTietDonHang; }
    public void setTopping(Topping topping) { this.topping = topping; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}