package com.highlands.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.DanhMucQuanLyDto;
import com.highlands.backend.entity.DanhMuc;
import com.highlands.backend.exception.QuanLyException;
import com.highlands.backend.repository.DanhMucRepository;
import com.highlands.backend.repository.SanPhamRepository;

@Service
public class DanhMucQuanLyService {

    private static final String DANG_BAN = "DANG_BAN";
    private static final String NGUNG_BAN = "NGUNG_BAN";

    private final DanhMucRepository danhMucRepository;
    private final SanPhamRepository sanPhamRepository;

    public DanhMucQuanLyService(DanhMucRepository danhMucRepository, SanPhamRepository sanPhamRepository) {
        this.danhMucRepository = danhMucRepository;
        this.sanPhamRepository = sanPhamRepository;
    }

    // trangThai để trống thì lấy tất cả
    @Transactional(readOnly = true)
    public List<DanhMucQuanLyDto> layDanhSach(String trangThai) {
        return danhMucRepository.findAllByOrderByIdAsc().stream()
                .filter(d -> trangThai == null || trangThai.isBlank() || trangThai.trim().equals(d.getTrangThai()))
                .map(this::chuyenDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DanhMucQuanLyDto layTheoId(Long id) {
        return chuyenDto(timTheoId(id));
    }

    @Transactional
    public DanhMucQuanLyDto them(DanhMucQuanLyDto yeuCau) {
        DanhMuc danhMuc = new DanhMuc();
        apDung(danhMuc, yeuCau, 0L);
        LocalDateTime bayGio = LocalDateTime.now();
        danhMuc.setCreatedAt(bayGio);
        danhMuc.setUpdatedAt(bayGio);
        return chuyenDto(luu(danhMuc));
    }

    @Transactional
    public DanhMucQuanLyDto sua(Long id, DanhMucQuanLyDto yeuCau) {
        DanhMuc danhMuc = timTheoId(id);
        apDung(danhMuc, yeuCau, id);
        danhMuc.setUpdatedAt(LocalDateTime.now());
        return chuyenDto(luu(danhMuc));
    }

    /** "Xóa" = ngừng bán. Không xóa dòng vì sản phẩm và đơn cũ còn tham chiếu tới. */
    @Transactional
    public DanhMucQuanLyDto ngungBan(Long id) {
        DanhMuc danhMuc = timTheoId(id);
        kiemTraKhongConSanPhamDangBan(danhMuc.getId());
        danhMuc.setTrangThai(NGUNG_BAN);
        danhMuc.setUpdatedAt(LocalDateTime.now());
        return chuyenDto(luu(danhMuc));
    }

    // idHienTai = id đang sửa (0 khi thêm mới), dùng để kiểm tra trùng tên
    private void apDung(DanhMuc danhMuc, DanhMucQuanLyDto yeuCau, Long idHienTai) {
        String ten = KiemTra.chuoiBatBuoc(yeuCau.tenDanhMuc(), "Tên danh mục", 100);
        String moTa = KiemTra.chuoiTuyChon(yeuCau.moTa(), "Mô tả", 255);
        // Không gửi trạng thái: thêm mới thì mặc định DANG_BAN, sửa thì giữ nguyên trạng thái cũ
        String macDinh = danhMuc.getTrangThai() == null ? DANG_BAN : danhMuc.getTrangThai();
        String trangThai = KiemTra.trangThai(yeuCau.trangThai(), macDinh, DANG_BAN, NGUNG_BAN);

        if (danhMucRepository.existsByTenDanhMucIgnoreCaseAndIdNot(ten, idHienTai)) {
            throw QuanLyException.saiDuLieu("Danh mục \"" + ten + "\" đã tồn tại");
        }
        if (NGUNG_BAN.equals(trangThai) && DANG_BAN.equals(danhMuc.getTrangThai())) {
            kiemTraKhongConSanPhamDangBan(danhMuc.getId());
        }

        danhMuc.setTenDanhMuc(ten);
        danhMuc.setMoTa(moTa);
        danhMuc.setTrangThai(trangThai);
    }

    private void kiemTraKhongConSanPhamDangBan(Long danhMucId) {
        if (sanPhamRepository.existsByDanhMuc_IdAndTrangThai(danhMucId, DANG_BAN)) {
            throw QuanLyException.saiDuLieu("Danh mục đang có sản phẩm đang bán, hãy ngừng bán hoặc chuyển sản phẩm trước");
        }
    }

    private DanhMuc luu(DanhMuc danhMuc) {
        try {
            return danhMucRepository.saveAndFlush(danhMuc);
        } catch (DataIntegrityViolationException e) {
            // Cột ten_danh_muc là UNIQUE và không phân biệt hoa thường, không phân biệt dấu
            throw QuanLyException.saiDuLieu("Tên danh mục đã tồn tại (không phân biệt hoa thường và dấu)");
        }
    }

    private DanhMuc timTheoId(Long id) {
        return danhMucRepository.findById(id)
                .orElseThrow(() -> QuanLyException.khongTimThay("Không tìm thấy danh mục id = " + id));
    }

    private DanhMucQuanLyDto chuyenDto(DanhMuc d) {
        return new DanhMucQuanLyDto(d.getId(), d.getTenDanhMuc(), d.getMoTa(), d.getTrangThai());
    }
}