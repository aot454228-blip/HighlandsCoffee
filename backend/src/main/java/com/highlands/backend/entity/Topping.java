package com.highlands.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "topping")
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_topping", nullable = false, unique = true, length = 100)
    private String tenTopping;

    @Column(name = "gia_ban", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaBan;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    public Long getId() { return id; }
    public String getTenTopping() { return tenTopping; }
    public BigDecimal getGiaBan() { return giaBan; }
    public String getHinhAnh() { return hinhAnh; }
    public String getTrangThai() { return trangThai; }

    // --- Setter cho phần quản lý (bổ sung) ---
    public void setTenTopping(String tenTopping) { this.tenTopping = tenTopping; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
