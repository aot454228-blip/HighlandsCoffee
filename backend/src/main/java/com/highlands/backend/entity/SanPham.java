package com.highlands.backend.entity;

import java.util.ArrayList;
import java.util.List;

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
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_san_pham", nullable = false, unique = true, length = 20)
    private String maSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danh_muc_id", nullable = false)
    private DanhMuc danhMuc;

    @Column(name = "ten_san_pham", nullable = false, length = 150)
    private String tenSanPham;

    @Column(name = "mo_ta", length = 1000)
    private String moTa;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    // Danh sách giá theo từng size của sản phẩm này
    @OneToMany(mappedBy = "sanPham")
    private List<SanPhamKichThuoc> cacGiaTheoSize = new ArrayList<>();

    public Long getId() { return id; }
    public String getMaSanPham() { return maSanPham; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public String getTenSanPham() { return tenSanPham; }
    public String getMoTa() { return moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public String getTrangThai() { return trangThai; }
    public List<SanPhamKichThuoc> getCacGiaTheoSize() { return cacGiaTheoSize; }
}