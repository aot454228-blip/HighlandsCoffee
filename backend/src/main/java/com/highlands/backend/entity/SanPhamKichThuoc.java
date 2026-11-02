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
@Table(name = "san_pham_kich_thuoc")
public class SanPhamKichThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kich_thuoc_id", nullable = false)
    private KichThuoc kichThuoc;

    @Column(name = "gia_ban", nullable = false, precision = 12, scale = 0)
    private BigDecimal giaBan;

    public Long getId() { return id; }
    public SanPham getSanPham() { return sanPham; }
    public KichThuoc getKichThuoc() { return kichThuoc; }
    public BigDecimal getGiaBan() { return giaBan; }

    // --- Setter cho phần quản lý (bổ sung) ---
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }
    public void setKichThuoc(KichThuoc kichThuoc) { this.kichThuoc = kichThuoc; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }
}
