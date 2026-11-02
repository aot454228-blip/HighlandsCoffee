package com.highlands.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.KhuyenMaiQuanLyDto;
import com.highlands.backend.entity.KhuyenMai;
import com.highlands.backend.exception.QuanLyException;
import com.highlands.backend.repository.KhuyenMaiRepository;

@Service
public class KhuyenMaiQuanLyService {

    private static final String PHAN_TRAM = "PHAN_TRAM";
    private static final String SO_TIEN = "SO_TIEN";
    private static final String HOAT_DONG = "HOAT_DONG";
    private static final String TAM_DUNG = "TAM_DUNG";

    private final KhuyenMaiRepository khuyenMaiRepository;

    public KhuyenMaiQuanLyService(KhuyenMaiRepository khuyenMaiRepository) {
        this.khuyenMaiRepository = khuyenMaiRepository;
    }

    // Tìm theo tên và/hoặc trạng thái; không truyền gì thì lấy tất cả
    @Transactional(readOnly = true)
    public List<KhuyenMaiQuanLyDto> layDanhSach(String tuKhoa, String trangThai) {
        List<KhuyenMai> danhSach = (tuKhoa == null || tuKhoa.isBlank())
                ? khuyenMaiRepository.findAllByOrderByIdAsc()
                : khuyenMaiRepository.findByTenKhuyenMaiContainingIgnoreCaseOrderByIdAsc(tuKhoa.trim());
        return danhSach.stream()
                .filter(k -> trangThai == null || trangThai.isBlank() || trangThai.trim().equals(k.getTrangThai()))
                .map(this::chuyenDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public KhuyenMaiQuanLyDto layTheoId(Long id) {
        return chuyenDto(timTheoId(id));
    }

    @Transactional
    public KhuyenMaiQuanLyDto them(KhuyenMaiQuanLyDto yeuCau) {
        KhuyenMai khuyenMai = new KhuyenMai();
        apDung(khuyenMai, yeuCau, 0L);
        return chuyenDto(luu(khuyenMai));
    }

    @Transactional
    public KhuyenMaiQuanLyDto sua(Long id, KhuyenMaiQuanLyDto yeuCau) {
        KhuyenMai khuyenMai = timTheoId(id);
        apDung(khuyenMai, yeuCau, id);
        return chuyenDto(luu(khuyenMai));
    }

    /** "Xóa" = tạm dừng: đơn hàng cũ còn tham chiếu khuyến mãi này. */
    @Transactional
    public KhuyenMaiQuanLyDto tamDung(Long id) {
        KhuyenMai khuyenMai = timTheoId(id);
        khuyenMai.setTrangThai(TAM_DUNG);
        return chuyenDto(luu(khuyenMai));
    }

    // Các ràng buộc ở đây khớp với các CHECK trong bảng khuyen_mai của MySQL
    private void apDung(KhuyenMai khuyenMai, KhuyenMaiQuanLyDto yeuCau, Long idHienTai) {
        String ten = KiemTra.chuoiBatBuoc(yeuCau.tenKhuyenMai(), "Tên khuyến mãi", 150);

        // Mã: có thể để trống (tự áp dụng); nếu có thì chỉ gồm chữ không dấu, số, - hoặc _
        String ma = KiemTra.chuoiTuyChon(yeuCau.maCode(), "Mã khuyến mãi", 30);
        if (ma != null) {
            ma = ma.toUpperCase();
            if (!ma.matches("[A-Z0-9_-]+")) {
                throw QuanLyException.saiDuLieu("Mã chỉ gồm chữ không dấu, số, dấu - hoặc _ (tối đa 30 ký tự)");
            }
            if (khuyenMaiRepository.existsByMaCodeIgnoreCaseAndIdNot(ma, idHienTai)) {
                throw QuanLyException.saiDuLieu("Mã \"" + ma + "\" đã tồn tại");
            }
        }

        // Loại giảm: để trống thì coi như giảm theo %. GIAM_TIEN (tên cũ) được hiểu là SO_TIEN
        String loai = (yeuCau.loaiGiam() == null || yeuCau.loaiGiam().isBlank())
                ? PHAN_TRAM : yeuCau.loaiGiam().trim().toUpperCase();
        if (loai.equals("GIAM_TIEN")) {
            loai = SO_TIEN;
        }
        if (!loai.equals(PHAN_TRAM) && !loai.equals(SO_TIEN)) {
            throw QuanLyException.saiDuLieu("Loại giảm phải là PHAN_TRAM hoặc SO_TIEN");
        }

        BigDecimal giaTri = yeuCau.giaTriGiam();
        if (giaTri == null || giaTri.signum() <= 0) {
            throw QuanLyException.saiDuLieu("Giá trị giảm phải lớn hơn 0");
        }
        BigDecimal giamToiDa = null;
        if (loai.equals(PHAN_TRAM)) {
            if (giaTri.compareTo(new BigDecimal("100")) > 0) {
                throw QuanLyException.saiDuLieu("Phần trăm giảm không được vượt quá 100");
            }
            if (giaTri.stripTrailingZeros().scale() > 2) {
                throw QuanLyException.saiDuLieu("Phần trăm giảm tối đa 2 chữ số thập phân");
            }
            if (yeuCau.giamToiDa() != null) {
                giamToiDa = KiemTra.soTienNguyen(yeuCau.giamToiDa(), "Mức giảm tối đa", false);
            }
        } else {
            giaTri = KiemTra.soTienNguyen(giaTri, "Số tiền giảm", false);
        }

        BigDecimal donToiThieu = yeuCau.donToiThieu() == null
                ? BigDecimal.ZERO
                : KiemTra.soTienNguyen(yeuCau.donToiThieu(), "Đơn tối thiểu", true);

        if (yeuCau.ngayBatDau() == null || yeuCau.ngayKetThuc() == null) {
            throw QuanLyException.saiDuLieu("Phải nhập ngày bắt đầu và ngày kết thúc");
        }
        if (yeuCau.ngayKetThuc().isBefore(yeuCau.ngayBatDau())) {
            throw QuanLyException.saiDuLieu("Ngày kết thúc không được trước ngày bắt đầu");
        }

        Integer luotToiDa = yeuCau.soLuotToiDa();
        if (luotToiDa != null) {
            if (luotToiDa <= 0) {
                throw QuanLyException.saiDuLieu("Số lượt tối đa phải lớn hơn 0 (để trống nếu không giới hạn)");
            }
            if (luotToiDa < khuyenMai.getSoLuotDaDung()) {
                throw QuanLyException.saiDuLieu("Số lượt tối đa không được nhỏ hơn số lượt đã dùng ("
                        + khuyenMai.getSoLuotDaDung() + ")");
            }
        }

        String hinhAnh = KiemTra.chuoiTuyChon(yeuCau.hinhAnh(), "Hình ảnh", 255);
        String macDinh = khuyenMai.getTrangThai() == null ? HOAT_DONG : khuyenMai.getTrangThai();
        String trangThai = KiemTra.trangThai(yeuCau.trangThai(), macDinh, HOAT_DONG, TAM_DUNG);

        khuyenMai.setTenKhuyenMai(ten);
        khuyenMai.setMaCode(ma);
        khuyenMai.setLoaiGiam(loai);
        khuyenMai.setGiaTriGiam(giaTri);
        khuyenMai.setGiamToiDa(giamToiDa);
        khuyenMai.setDonToiThieu(donToiThieu);
        khuyenMai.setNgayBatDau(yeuCau.ngayBatDau());
        khuyenMai.setNgayKetThuc(yeuCau.ngayKetThuc());
        khuyenMai.setSoLuotToiDa(luotToiDa);
        khuyenMai.setHinhAnh(hinhAnh);
        khuyenMai.setTrangThai(trangThai);
    }

    private KhuyenMai luu(KhuyenMai khuyenMai) {
        try {
            return khuyenMaiRepository.saveAndFlush(khuyenMai);
        } catch (DataIntegrityViolationException e) {
            throw QuanLyException.saiDuLieu("Không lưu được khuyến mãi (mã khuyến mãi có thể đã tồn tại)");
        }
    }

    private KhuyenMai timTheoId(Long id) {
        return khuyenMaiRepository.findById(id)
                .orElseThrow(() -> QuanLyException.khongTimThay("Không tìm thấy khuyến mãi id = " + id));
    }

    private KhuyenMaiQuanLyDto chuyenDto(KhuyenMai k) {
        return new KhuyenMaiQuanLyDto(k.getId(), k.getTenKhuyenMai(), k.getMaCode(), k.getLoaiGiam(),
                k.getGiaTriGiam(), k.getGiamToiDa(), k.getDonToiThieu(), k.getNgayBatDau(), k.getNgayKetThuc(),
                k.getSoLuotToiDa(), k.getSoLuotDaDung(), k.getHinhAnh(), k.getTrangThai());
    }
}