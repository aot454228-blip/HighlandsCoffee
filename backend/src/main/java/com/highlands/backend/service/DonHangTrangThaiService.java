package com.highlands.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.DoiTrangThaiRequest;
import com.highlands.backend.dto.DonHangResponse;
import com.highlands.backend.dto.DonHangTomTatDto;
import com.highlands.backend.entity.DonHang;
import com.highlands.backend.entity.LichSuTrangThaiDonHang;
import com.highlands.backend.entity.TaiKhoan;
import com.highlands.backend.exception.DonHangKhongHopLeException;
import com.highlands.backend.repository.DonHangRepository;
import com.highlands.backend.repository.HoaDonRepository;
import com.highlands.backend.repository.LichSuTrangThaiRepository;
import com.highlands.backend.repository.TaiKhoanRepository;

@Service
public class DonHangTrangThaiService {

    // Mỗi trạng thái chỉ được chuyển sang đúng MỘT trạng thái kế tiếp
    private static final Map<String, String> BUOC_KE_TIEP = Map.of(
            "MOI_TAO", "DA_XAC_NHAN",
            "DA_XAC_NHAN", "DANG_CHUAN_BI",
            "DANG_CHUAN_BI", "SAN_SANG",
            "SAN_SANG", "HOAN_THANH");

    private static final Set<String> TRANG_THAI_KET_THUC = Set.of("HOAN_THANH", "DA_HUY");

    private final DonHangRepository donHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final LichSuTrangThaiRepository lichSuRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public DonHangTrangThaiService(DonHangRepository donHangRepository, HoaDonRepository hoaDonRepository,
            LichSuTrangThaiRepository lichSuRepository, TaiKhoanRepository taiKhoanRepository) {
        this.donHangRepository = donHangRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.lichSuRepository = lichSuRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Transactional(readOnly = true)
    public List<DonHangTomTatDto> layDanhSach(String trangThai) {
        List<DonHang> cacDon = (trangThai == null || trangThai.isBlank())
                ? donHangRepository.findTop100ByOrderByThoiGianDatDesc()
                : donHangRepository.findTop100ByTrangThaiOrderByThoiGianDatAsc(trangThai.trim());

        List<DonHangTomTatDto> ketQua = new ArrayList<>();
        for (DonHang d : cacDon) {
            ketQua.add(new DonHangTomTatDto(d.getId(), d.getMaDonHang(), d.getTrangThai(),
                    d.getHinhThucPhucVu(), d.getTongThanhToan(), d.getThoiGianDat()));
        }
        return ketQua;
    }

    @Transactional
    public DonHangResponse doiTrangThai(Long donHangId, DoiTrangThaiRequest request) {
        if (request == null || request.taiKhoanId() == null) {
            throw new DonHangKhongHopLeException("Thiếu thông tin nhân viên");
        }
        if (request.trangThaiMoi() == null || request.trangThaiMoi().isBlank()) {
            throw new DonHangKhongHopLeException("Thiếu trạng thái mới");
        }

        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new DonHangKhongHopLeException("Không tìm thấy đơn hàng"));
        TaiKhoan nhanVien = taiKhoanRepository.findById(request.taiKhoanId())
                .orElseThrow(() -> new DonHangKhongHopLeException("Không tìm thấy tài khoản nhân viên"));
        if ("BI_KHOA".equals(nhanVien.getTrangThai())) {
            throw new DonHangKhongHopLeException("Tài khoản đã bị khóa");
        }

        String cu = donHang.getTrangThai();
        String moi = request.trangThaiMoi().trim();
        String ghiChu = null;

        if (TRANG_THAI_KET_THUC.contains(cu)) {
            throw new DonHangKhongHopLeException("Đơn hàng đã kết thúc, không thể đổi trạng thái");
        }

        LocalDateTime bayGio = LocalDateTime.now();

        if ("DA_HUY".equals(moi)) {
            if (request.lyDo() == null || request.lyDo().isBlank()) {
                throw new DonHangKhongHopLeException("Hủy đơn phải ghi lý do");
            }
            if (hoaDonRepository.existsByDonHangId(donHangId)) {
                throw new DonHangKhongHopLeException("Đơn đã thanh toán, không thể hủy");
            }
            ghiChu = request.lyDo().trim();
            if (ghiChu.length() > 255) {
                throw new DonHangKhongHopLeException("Lý do hủy tối đa 255 ký tự");
            }
            donHang.setThoiGianHuy(bayGio);
            donHang.setLyDoHuy(ghiChu);
        } else {
            if (!moi.equals(BUOC_KE_TIEP.get(cu))) {
                throw new DonHangKhongHopLeException(
                        "Không thể chuyển từ " + cu + " sang " + moi
                        + (BUOC_KE_TIEP.containsKey(cu) ? " (bước tiếp theo phải là " + BUOC_KE_TIEP.get(cu) + ")" : ""));
            }
            if ("HOAN_THANH".equals(moi)) {
                if (!hoaDonRepository.existsByDonHangId(donHangId)) {
                    throw new DonHangKhongHopLeException("Đơn chưa thanh toán, không thể hoàn thành");
                }
                donHang.setThoiGianHoanThanh(bayGio);
            }
        }

        donHang.setTrangThai(moi);
        donHangRepository.save(donHang);

        LichSuTrangThaiDonHang lichSu = new LichSuTrangThaiDonHang();
        lichSu.setDonHang(donHang);
        lichSu.setTrangThaiCu(cu);
        lichSu.setTrangThaiMoi(moi);
        lichSu.setTaiKhoan(nhanVien);
        lichSu.setGhiChu(ghiChu);
        lichSuRepository.save(lichSu);

        return new DonHangResponse(donHang.getId(), donHang.getMaDonHang(), donHang.getTrangThai(),
                donHang.getTongTienHang(), donHang.getTongThanhToan(), donHang.getThoiGianDat());
    }
}