package com.highlands.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Table(name = "don_hang")
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_don_hang", nullable = false, unique = true, length = 30)
    private String maDonHang;

    // Nhân viên tiếp nhận đơn
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_khoan_id")
    private TaiKhoan taiKhoan;

    @Column(name = "kenh_dat", nullable = false, length = 20)
    private String kenhDat;

    @Column(name = "hinh_thuc_phuc_vu", nullable = false, length = 20)
    private String hinhThucPhucVu;

    @Column(name = "trang_thai", nullable = false, length = 20)
    private String trangThai;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "tong_tien_hang", nullable = false, precision = 14, scale = 0)
    private BigDecimal tongTienHang = BigDecimal.ZERO;

    @Column(name = "tien_giam_khuyen_mai", nullable = false, precision = 14, scale = 0)
    private BigDecimal tienGiamKhuyenMai = BigDecimal.ZERO;

    @Column(name = "diem_su_dung", nullable = false)
    private int diemSuDung = 0;

    @Column(name = "tien_giam_diem", nullable = false, precision = 14, scale = 0)
    private BigDecimal tienGiamDiem = BigDecimal.ZERO;

    @Column(name = "tong_thanh_toan", nullable = false, precision = 14, scale = 0)
    private BigDecimal tongThanhToan = BigDecimal.ZERO;

    @Column(name = "thoi_gian_dat", nullable = false)
    private LocalDateTime thoiGianDat;

    // Lưu đơn thì tự lưu luôn các dòng chi tiết (cascade)
    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDonHang> cacChiTiet = new ArrayList<>();

    /** Thêm một dòng chi tiết và gắn ngược về đơn này. */
    public void themChiTiet(ChiTietDonHang chiTiet) {
        cacChiTiet.add(chiTiet);
        chiTiet.setDonHang(this);
    }

    public Long getId() { return id; }
    public String getMaDonHang() { return maDonHang; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public String getKenhDat() { return kenhDat; }
    public String getHinhThucPhucVu() { return hinhThucPhucVu; }
    public String getTrangThai() { return trangThai; }
    public String getGhiChu() { return ghiChu; }
    public BigDecimal getTongTienHang() { return tongTienHang; }
    public BigDecimal getTienGiamKhuyenMai() { return tienGiamKhuyenMai; }
    public int getDiemSuDung() { return diemSuDung; }
    public BigDecimal getTienGiamDiem() { return tienGiamDiem; }
    public BigDecimal getTongThanhToan() { return tongThanhToan; }
    public LocalDateTime getThoiGianDat() { return thoiGianDat; }
    public List<ChiTietDonHang> getCacChiTiet() { return cacChiTiet; }

    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public void setTaiKhoan(TaiKhoan taiKhoan) { this.taiKhoan = taiKhoan; }
    public void setKenhDat(String kenhDat) { this.kenhDat = kenhDat; }
    public void setHinhThucPhucVu(String hinhThucPhucVu) { this.hinhThucPhucVu = hinhThucPhucVu; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public void setTongTienHang(BigDecimal tongTienHang) { this.tongTienHang = tongTienHang; }
    public void setTongThanhToan(BigDecimal tongThanhToan) { this.tongThanhToan = tongThanhToan; }
    public void setThoiGianDat(LocalDateTime thoiGianDat) { this.thoiGianDat = thoiGianDat; }
}