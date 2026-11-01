package com.highlands.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.DonHangResponse;
import com.highlands.backend.dto.MonDatRequest;
import com.highlands.backend.dto.TaoDonHangRequest;
import com.highlands.backend.entity.ChiTietDonHang;
import com.highlands.backend.entity.ChiTietDonHangTopping;
import com.highlands.backend.entity.DonHang;
import com.highlands.backend.entity.SanPham;
import com.highlands.backend.entity.SanPhamKichThuoc;
import com.highlands.backend.entity.TaiKhoan;
import com.highlands.backend.entity.Topping;
import com.highlands.backend.exception.DonHangKhongHopLeException;
import com.highlands.backend.repository.DonHangRepository;
import com.highlands.backend.repository.SanPhamRepository;
import com.highlands.backend.repository.TaiKhoanRepository;
import com.highlands.backend.repository.ToppingRepository;

@Service
public class DonHangService {

    private static final String DANG_BAN = "DANG_BAN";
    private static final int SO_LUONG_TOI_DA = 50;
    private static final DateTimeFormatter DINH_DANG_MA = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final DonHangRepository donHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final SanPhamRepository sanPhamRepository;
    private final ToppingRepository toppingRepository;

    public DonHangService(DonHangRepository donHangRepository, TaiKhoanRepository taiKhoanRepository,
            SanPhamRepository sanPhamRepository, ToppingRepository toppingRepository) {
        this.donHangRepository = donHangRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.toppingRepository = toppingRepository;
    }

    // @Transactional: hoặc lưu thành công TOÀN BỘ đơn, hoặc có lỗi thì không lưu gì cả
    @Transactional
    public DonHangResponse taoDonTaiQuay(TaoDonHangRequest request) {
        if (request == null || request.cacMon() == null || request.cacMon().isEmpty()) {
            throw new DonHangKhongHopLeException("Đơn hàng chưa có món nào");
        }
        if (request.taiKhoanId() == null) {
            throw new DonHangKhongHopLeException("Thiếu thông tin nhân viên tạo đơn");
        }

        TaiKhoan nhanVien = taiKhoanRepository.findById(request.taiKhoanId())
                .orElseThrow(() -> new DonHangKhongHopLeException("Không tìm thấy tài khoản nhân viên"));
        if ("BI_KHOA".equals(nhanVien.getTrangThai())) {
            throw new DonHangKhongHopLeException("Tài khoản đã bị khóa");
        }

        String hinhThuc = request.hinhThucPhucVu();
        if (hinhThuc == null || hinhThuc.isBlank()) {
            hinhThuc = "TAI_CHO";
        }
        if (!hinhThuc.equals("TAI_CHO") && !hinhThuc.equals("MANG_DI")) {
            throw new DonHangKhongHopLeException("Đơn tại quầy chỉ có thể là TAI_CHO hoặc MANG_DI");
        }

        LocalDateTime bayGio = LocalDateTime.now();
        DonHang donHang = new DonHang();
        donHang.setMaDonHang("DH" + bayGio.format(DINH_DANG_MA));
        donHang.setTaiKhoan(nhanVien);
        donHang.setKenhDat("TAI_QUAY");
        donHang.setHinhThucPhucVu(hinhThuc);
        donHang.setTrangThai("MOI_TAO");
        donHang.setGhiChu(request.ghiChu());
        donHang.setThoiGianDat(bayGio);

        BigDecimal tongTienHang = BigDecimal.ZERO;
        for (MonDatRequest mon : request.cacMon()) {
            ChiTietDonHang chiTiet = taoChiTiet(mon);
            donHang.themChiTiet(chiTiet);
            tongTienHang = tongTienHang.add(chiTiet.getThanhTien());
        }

        // Chưa có khuyến mãi/điểm nên tổng thanh toán = tổng tiền hàng
        // (CSDL có ràng buộc: tong_thanh_toan = tong_tien_hang - giảm giá)
        donHang.setTongTienHang(tongTienHang);
        donHang.setTongThanhToan(tongTienHang);

        DonHang daLuu = donHangRepository.save(donHang);
        return new DonHangResponse(daLuu.getId(), daLuu.getMaDonHang(), daLuu.getTrangThai(),
                daLuu.getTongTienHang(), daLuu.getTongThanhToan(), daLuu.getThoiGianDat());
    }

    /** Tạo một dòng chi tiết: kiểm tra món, tra giá size và giá topping trong database. */
    private ChiTietDonHang taoChiTiet(MonDatRequest mon) {
        if (mon.sanPhamId() == null || mon.kichThuocId() == null) {
            throw new DonHangKhongHopLeException("Món thiếu sản phẩm hoặc kích thước");
        }
        if (mon.soLuong() <= 0 || mon.soLuong() > SO_LUONG_TOI_DA) {
            throw new DonHangKhongHopLeException("Số lượng mỗi món phải từ 1 đến " + SO_LUONG_TOI_DA);
        }

        SanPham sanPham = sanPhamRepository.findById(mon.sanPhamId())
                .filter(sp -> DANG_BAN.equals(sp.getTrangThai()))
                .orElseThrow(() -> new DonHangKhongHopLeException(
                        "Sản phẩm không tồn tại hoặc ngừng bán (id " + mon.sanPhamId() + ")"));

        SanPhamKichThuoc giaSize = sanPham.getCacGiaTheoSize().stream()
                .filter(g -> g.getKichThuoc().getId().equals(mon.kichThuocId()))
                .findFirst()
                .orElseThrow(() -> new DonHangKhongHopLeException(
                        "Sản phẩm " + sanPham.getTenSanPham() + " không có size này"));

        ChiTietDonHang chiTiet = new ChiTietDonHang();
        chiTiet.setSanPham(sanPham);
        chiTiet.setKichThuoc(giaSize.getKichThuoc());
        chiTiet.setTenSanPham(sanPham.getTenSanPham());
        chiTiet.setDonGia(giaSize.getGiaBan());
        chiTiet.setSoLuong(mon.soLuong());
        chiTiet.setMucDuong(mon.mucDuong());
        chiTiet.setMucDa(mon.mucDa());
        chiTiet.setGhiChu(mon.ghiChu());

        // Dùng Set để loại topping bị chọn trùng (CSDL không cho trùng topping trong một ly)
        Set<Long> idTopping = new LinkedHashSet<>();
        if (mon.cacToppingId() != null) {
            idTopping.addAll(mon.cacToppingId());
        }

        BigDecimal tongTopping = BigDecimal.ZERO;
        for (Long idTp : idTopping) {
            if (idTp == null) {
                throw new DonHangKhongHopLeException("Topping không hợp lệ");
            }
            Topping topping = toppingRepository.findById(idTp)
                    .filter(t -> DANG_BAN.equals(t.getTrangThai()))
                    .orElseThrow(() -> new DonHangKhongHopLeException(
                            "Topping không tồn tại hoặc ngừng bán (id " + idTp + ")"));

            ChiTietDonHangTopping dongTopping = new ChiTietDonHangTopping();
            dongTopping.setTopping(topping);
            dongTopping.setSoLuong(1);
            dongTopping.setDonGia(topping.getGiaBan());
            chiTiet.themTopping(dongTopping);

            tongTopping = tongTopping.add(topping.getGiaBan());
        }

        // Thành tiền = (giá size + giá topping) x số lượng
        chiTiet.setThanhTien(giaSize.getGiaBan().add(tongTopping)
                .multiply(BigDecimal.valueOf(mon.soLuong())));
        return chiTiet;
    }
}