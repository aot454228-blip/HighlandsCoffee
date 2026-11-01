package com.highlands.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.DongHoaDonDto;
import com.highlands.backend.dto.HoaDonResponse;
import com.highlands.backend.dto.ThanhToanRequest;
import com.highlands.backend.entity.ChiTietDonHang;
import com.highlands.backend.entity.ChiTietDonHangTopping;
import com.highlands.backend.entity.DonHang;
import com.highlands.backend.entity.HoaDon;
import com.highlands.backend.entity.TaiKhoan;
import com.highlands.backend.entity.ThanhToan;
import com.highlands.backend.exception.DonHangKhongHopLeException;
import com.highlands.backend.repository.DonHangRepository;
import com.highlands.backend.repository.HoaDonRepository;
import com.highlands.backend.repository.TaiKhoanRepository;
import com.highlands.backend.repository.ThanhToanRepository;

@Service
public class ThanhToanService {

    private static final DateTimeFormatter DINH_DANG_MA = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final DonHangRepository donHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public ThanhToanService(DonHangRepository donHangRepository, HoaDonRepository hoaDonRepository,
            ThanhToanRepository thanhToanRepository, TaiKhoanRepository taiKhoanRepository) {
        this.donHangRepository = donHangRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.thanhToanRepository = thanhToanRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    // Tạo hóa đơn + ghi thanh toán trong cùng một giao dịch: lỗi thì không lưu gì cả
    @Transactional
    public HoaDonResponse thanhToan(Long donHangId, ThanhToanRequest request) {
        if (request == null || request.taiKhoanId() == null) {
            throw new DonHangKhongHopLeException("Thiếu thông tin nhân viên thu tiền");
        }

        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new DonHangKhongHopLeException("Không tìm thấy đơn hàng"));
        if ("DA_HUY".equals(donHang.getTrangThai())) {
            throw new DonHangKhongHopLeException("Đơn hàng đã hủy, không thể thanh toán");
        }
        if (hoaDonRepository.existsByDonHangId(donHangId)) {
            throw new DonHangKhongHopLeException("Đơn hàng này đã thanh toán");
        }

        TaiKhoan thuNgan = taiKhoanRepository.findById(request.taiKhoanId())
                .orElseThrow(() -> new DonHangKhongHopLeException("Không tìm thấy tài khoản nhân viên"));
        if ("BI_KHOA".equals(thuNgan.getTrangThai())) {
            throw new DonHangKhongHopLeException("Tài khoản đã bị khóa");
        }

        // Số tiền cần trả lấy từ đơn hàng trong database, không tin số từ client
        BigDecimal soTien = donHang.getTongThanhToan();
        if (soTien.signum() <= 0) {
            throw new DonHangKhongHopLeException("Đơn hàng có số tiền bằng 0, không thể thanh toán");
        }

        String phuongThuc = request.phuongThuc();
        BigDecimal khachDua = null;
        BigDecimal tienThua = null;
        String maGiaoDich = null;

        if ("TIEN_MAT".equals(phuongThuc)) {
            if (request.soTienKhachDua() == null || request.soTienKhachDua().compareTo(soTien) < 0) {
                throw new DonHangKhongHopLeException("Số tiền khách đưa chưa đủ");
            }
            khachDua = request.soTienKhachDua();
            tienThua = khachDua.subtract(soTien);
        } else if ("CHUYEN_KHOAN".equals(phuongThuc)) {
            if (request.maGiaoDich() == null || request.maGiaoDich().isBlank()) {
                throw new DonHangKhongHopLeException("Chuyển khoản cần có mã giao dịch");
            }
            maGiaoDich = request.maGiaoDich().trim();
            if (maGiaoDich.length() > 50) {
                throw new DonHangKhongHopLeException("Mã giao dịch tối đa 50 ký tự");
            }
            if (thanhToanRepository.existsByMaGiaoDich(maGiaoDich)) {
                throw new DonHangKhongHopLeException("Mã giao dịch này đã được sử dụng");
            }
        } else {
            throw new DonHangKhongHopLeException("Phương thức thanh toán phải là TIEN_MAT hoặc CHUYEN_KHOAN");
        }

        LocalDateTime bayGio = LocalDateTime.now();

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHoaDon("HD" + bayGio.format(DINH_DANG_MA));
        hoaDon.setDonHang(donHang);
        hoaDon.setTaiKhoan(thuNgan);
        hoaDon.setThoiGianLap(bayGio);
        hoaDon.setSoTienPhaiTra(soTien);
        hoaDon.setTrangThai("DA_THANH_TOAN");
        hoaDonRepository.save(hoaDon);

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setTaiKhoan(thuNgan);
        thanhToan.setPhuongThuc(phuongThuc);
        thanhToan.setSoTien(soTien);
        thanhToan.setSoTienKhachDua(khachDua);
        thanhToan.setTienThua(tienThua);
        thanhToan.setMaGiaoDich(maGiaoDich);
        thanhToan.setTrangThai("THANH_CONG");
        thanhToan.setThoiGianThanhToan(bayGio);
        thanhToanRepository.save(thanhToan);

        return taoPhanHoi(hoaDon, thanhToan);
    }

    @Transactional(readOnly = true)
    public HoaDonResponse xemHoaDon(Long donHangId) {
        HoaDon hoaDon = hoaDonRepository.findByDonHangId(donHangId)
                .orElseThrow(() -> new DonHangKhongHopLeException("Đơn hàng này chưa có hóa đơn"));
        ThanhToan thanhToan = thanhToanRepository.findFirstByHoaDonId(hoaDon.getId())
                .orElseThrow(() -> new DonHangKhongHopLeException("Hóa đơn chưa có thông tin thanh toán"));
        return taoPhanHoi(hoaDon, thanhToan);
    }

    /** Gom hóa đơn + thanh toán + các dòng món thành dữ liệu trả về cho Swing. */
    private HoaDonResponse taoPhanHoi(HoaDon hoaDon, ThanhToan thanhToan) {
        DonHang donHang = hoaDon.getDonHang();

        List<DongHoaDonDto> cacDong = new ArrayList<>();
        for (ChiTietDonHang ct : donHang.getCacChiTiet()) {
            List<String> tenTopping = new ArrayList<>();
            for (ChiTietDonHangTopping t : ct.getCacTopping()) {
                tenTopping.add(t.getTopping().getTenTopping());
            }
            cacDong.add(new DongHoaDonDto(
                    ct.getTenSanPham(),
                    ct.getKichThuoc().getTenKichThuoc(),
                    ct.getSoLuong(),
                    ct.getDonGia(),
                    tenTopping,
                    ct.getThanhTien()));
        }

        return new HoaDonResponse(
                hoaDon.getId(),
                hoaDon.getMaHoaDon(),
                donHang.getMaDonHang(),
                hoaDon.getThoiGianLap(),
                hoaDon.getTrangThai(),
                thanhToan.getPhuongThuc(),
                donHang.getTongTienHang(),
                hoaDon.getSoTienPhaiTra(),
                thanhToan.getSoTienKhachDua(),
                thanhToan.getTienThua(),
                cacDong);
    }
}