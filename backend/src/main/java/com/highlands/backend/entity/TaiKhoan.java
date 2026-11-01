package com.highlands.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_dang_nhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau_ma_hoa", nullable = false, length = 100)
    private String matKhauMaHoa;

    @Column(name = "vai_tro", nullable = false, length = 20)
    private String vaiTro;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "lan_dang_nhap_cuoi")
    private LocalDateTime lanDangNhapCuoi;

    public Long getId() { return id; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhauMaHoa() { return matKhauMaHoa; }
    public String getVaiTro() { return vaiTro; }
    public String getTrangThai() { return trangThai; }
    public LocalDateTime getLanDangNhapCuoi() { return lanDangNhapCuoi; }

    public void setLanDangNhapCuoi(LocalDateTime lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }
}