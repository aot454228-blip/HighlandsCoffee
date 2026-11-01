package com.highlands.frontend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.highlands.frontend.dto.ToppingDto;

/** Một dòng trong giỏ hàng: một sản phẩm với size, topping, số lượng đã chọn. */
public class MonTrongGio {

    private final Long sanPhamId;
    private final Long kichThuocId;
    private final String tenSanPham;
    private final String tenKichThuoc;
    private final BigDecimal donGia;
    private final List<ToppingDto> cacTopping;
    private final String mucDuong;
    private final String mucDa;
    private final String ghiChu;
    private final int soLuong;

    public MonTrongGio(Long sanPhamId, Long kichThuocId, String tenSanPham, String tenKichThuoc,
            BigDecimal donGia, List<ToppingDto> cacTopping, String mucDuong, String mucDa,
            String ghiChu, int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        this.sanPhamId = sanPhamId;
        this.kichThuocId = kichThuocId;
        this.tenSanPham = tenSanPham;
        this.tenKichThuoc = tenKichThuoc;
        this.donGia = donGia;
        this.cacTopping = new ArrayList<>(cacTopping);
        this.mucDuong = mucDuong;
        this.mucDa = mucDa;
        this.ghiChu = ghiChu;
        this.soLuong = soLuong;
    }

    public Long getSanPhamId() { return sanPhamId; }
    public Long getKichThuocId() { return kichThuocId; }
    public String getTenSanPham() { return tenSanPham; }
    public String getTenKichThuoc() { return tenKichThuoc; }
    public BigDecimal getDonGia() { return donGia; }
    public List<ToppingDto> getCacTopping() { return new ArrayList<>(cacTopping); }
    public String getMucDuong() { return mucDuong; }
    public String getMucDa() { return mucDa; }
    public String getGhiChu() { return ghiChu; }
    public int getSoLuong() { return soLuong; }

    /** Tổng giá topping cho một ly. */
    public BigDecimal tongGiaTopping() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ToppingDto t : cacTopping) {
            tong = tong.add(t.giaBan());
        }
        return tong;
    }

    /** Thành tiền = (giá size + giá topping) x số lượng. */
    public BigDecimal thanhTien() {
        return donGia.add(tongGiaTopping()).multiply(BigDecimal.valueOf(soLuong));
    }

    public String moTaTopping() {
        if (cacTopping.isEmpty()) {
            return "-";
        }
        return cacTopping.stream().map(ToppingDto::tenTopping).collect(Collectors.joining(", "));
    }

    public String moTaTuyChon() {
        if (mucDuong == null || mucDa == null) {
            return "-";
        }
        return "Đường " + mucDuong + ", đá: " + mucDa;
    }
}