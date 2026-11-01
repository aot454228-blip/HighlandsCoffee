package com.highlands.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Mỗi lần đổi trạng thái đơn hàng ghi một dòng (cột created_at do MySQL tự điền). */
@Entity
@Table(name = "lich_su_trang_thai_don_hang")
public class LichSuTrangThaiDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_hang_id", nullable = false)
    private DonHang donHang;

    @Column(name = "trang_thai_cu", length = 20)
    private String trangThaiCu;

    @Column(name = "trang_thai_moi", nullable = false, length = 20)
    private String trangThaiMoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id")
    private TaiKhoan taiKhoan;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public Long getId() { return id; }
    public DonHang getDonHang() { return donHang; }
    public String getTrangThaiCu() { return trangThaiCu; }
    public String getTrangThaiMoi() { return trangThaiMoi; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public String getGhiChu() { return ghiChu; }

    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public void setTrangThaiCu(String trangThaiCu) { this.trangThaiCu = trangThaiCu; }
    public void setTrangThaiMoi(String trangThaiMoi) { this.trangThaiMoi = trangThaiMoi; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}