package com.highlands.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kich_thuoc")
public class KichThuoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_kich_thuoc", nullable = false, unique = true, length = 10)
    private String maKichThuoc;

    @Column(name = "ten_kich_thuoc", nullable = false, length = 50)
    private String tenKichThuoc;

    @Column(name = "he_so_dinh_muc", nullable = false, precision = 4, scale = 2)
    private BigDecimal heSoDinhMuc;

    public Long getId() { return id; }
    public String getMaKichThuoc() { return maKichThuoc; }
    public String getTenKichThuoc() { return tenKichThuoc; }
    public BigDecimal getHeSoDinhMuc() { return heSoDinhMuc; }
}