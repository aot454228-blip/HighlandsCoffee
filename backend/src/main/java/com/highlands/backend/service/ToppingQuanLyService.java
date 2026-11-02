package com.highlands.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.ToppingQuanLyDto;
import com.highlands.backend.entity.Topping;
import com.highlands.backend.exception.QuanLyException;
import com.highlands.backend.repository.ToppingRepository;

@Service
public class ToppingQuanLyService {

    private static final String DANG_BAN = "DANG_BAN";
    private static final String NGUNG_BAN = "NGUNG_BAN";

    private final ToppingRepository toppingRepository;

    public ToppingQuanLyService(ToppingRepository toppingRepository) {
        this.toppingRepository = toppingRepository;
    }

    @Transactional(readOnly = true)
    public List<ToppingQuanLyDto> layDanhSach(String trangThai) {
        return toppingRepository.findAllByOrderByIdAsc().stream()
                .filter(t -> trangThai == null || trangThai.isBlank() || trangThai.trim().equals(t.getTrangThai()))
                .map(this::chuyenDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ToppingQuanLyDto layTheoId(Long id) {
        return chuyenDto(timTheoId(id));
    }

    @Transactional
    public ToppingQuanLyDto them(ToppingQuanLyDto yeuCau) {
        Topping topping = new Topping();
        apDung(topping, yeuCau, 0L);
        return chuyenDto(luu(topping));
    }

    @Transactional
    public ToppingQuanLyDto sua(Long id, ToppingQuanLyDto yeuCau) {
        Topping topping = timTheoId(id);
        apDung(topping, yeuCau, id);
        return chuyenDto(luu(topping));
    }

    /** "Xóa" = ngừng bán: đơn cũ vẫn tham chiếu được topping này. */
    @Transactional
    public ToppingQuanLyDto ngungBan(Long id) {
        Topping topping = timTheoId(id);
        topping.setTrangThai(NGUNG_BAN);
        return chuyenDto(luu(topping));
    }

    private void apDung(Topping topping, ToppingQuanLyDto yeuCau, Long idHienTai) {
        String ten = KiemTra.chuoiBatBuoc(yeuCau.tenTopping(), "Tên topping", 100);
        BigDecimal gia = KiemTra.soTienNguyen(yeuCau.giaBan(), "Giá topping", true);
        String hinhAnh = KiemTra.chuoiTuyChon(yeuCau.hinhAnh(), "Hình ảnh", 255);
        String macDinh = topping.getTrangThai() == null ? DANG_BAN : topping.getTrangThai();
        String trangThai = KiemTra.trangThai(yeuCau.trangThai(), macDinh, DANG_BAN, NGUNG_BAN);

        if (toppingRepository.existsByTenToppingIgnoreCaseAndIdNot(ten, idHienTai)) {
            throw QuanLyException.saiDuLieu("Topping \"" + ten + "\" đã tồn tại");
        }

        topping.setTenTopping(ten);
        topping.setGiaBan(gia);
        topping.setHinhAnh(hinhAnh);
        topping.setTrangThai(trangThai);
    }

    private Topping luu(Topping topping) {
        try {
            return toppingRepository.saveAndFlush(topping);
        } catch (DataIntegrityViolationException e) {
            throw QuanLyException.saiDuLieu("Tên topping đã tồn tại (không phân biệt hoa thường và dấu)");
        }
    }

    private Topping timTheoId(Long id) {
        return toppingRepository.findById(id)
                .orElseThrow(() -> QuanLyException.khongTimThay("Không tìm thấy topping id = " + id));
    }

    private ToppingQuanLyDto chuyenDto(Topping t) {
        return new ToppingQuanLyDto(t.getId(), t.getTenTopping(), t.getGiaBan(), t.getHinhAnh(), t.getTrangThai());
    }
}