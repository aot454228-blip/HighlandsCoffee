package com.highlands.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.GiaSizeRequest;
import com.highlands.backend.dto.KichThuocDto;
import com.highlands.backend.dto.KichThuocGiaDto;
import com.highlands.backend.dto.SanPhamQuanLyRequest;
import com.highlands.backend.dto.SanPhamQuanLyResponse;
import com.highlands.backend.entity.DanhMuc;
import com.highlands.backend.entity.KichThuoc;
import com.highlands.backend.entity.SanPham;
import com.highlands.backend.entity.SanPhamKichThuoc;
import com.highlands.backend.exception.QuanLyException;
import com.highlands.backend.repository.DanhMucRepository;
import com.highlands.backend.repository.KichThuocRepository;
import com.highlands.backend.repository.SanPhamKichThuocRepository;
import com.highlands.backend.repository.SanPhamRepository;

@Service
public class SanPhamQuanLyService {

    private static final String DANG_BAN = "DANG_BAN";
    private static final String NGUNG_BAN = "NGUNG_BAN";

    /** Một dòng giá đã kiểm tra hợp lệ: size + giá. */
    private record DongGia(KichThuoc kichThuoc, BigDecimal giaBan) { }

    private final SanPhamRepository sanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final KichThuocRepository kichThuocRepository;
    private final SanPhamKichThuocRepository giaRepository;

    public SanPhamQuanLyService(SanPhamRepository sanPhamRepository, DanhMucRepository danhMucRepository,
            KichThuocRepository kichThuocRepository, SanPhamKichThuocRepository giaRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.danhMucRepository = danhMucRepository;
        this.kichThuocRepository = kichThuocRepository;
        this.giaRepository = giaRepository;
    }

    // Tìm theo tên, theo danh mục, theo trạng thái, hoặc kết hợp; không truyền gì thì lấy tất cả
    @Transactional(readOnly = true)
    public List<SanPhamQuanLyResponse> layDanhSach(String tuKhoa, Long danhMucId, String trangThai) {
        boolean coTuKhoa = tuKhoa != null && !tuKhoa.isBlank();
        List<SanPham> danhSach;
        if (coTuKhoa && danhMucId != null) {
            danhSach = sanPhamRepository.findByTenSanPhamContainingIgnoreCaseAndDanhMuc_IdOrderByIdAsc(tuKhoa.trim(), danhMucId);
        } else if (coTuKhoa) {
            danhSach = sanPhamRepository.findByTenSanPhamContainingIgnoreCaseOrderByIdAsc(tuKhoa.trim());
        } else if (danhMucId != null) {
            danhSach = sanPhamRepository.findByDanhMuc_IdOrderByIdAsc(danhMucId);
        } else {
            danhSach = sanPhamRepository.findAllByOrderByIdAsc();
        }
        return danhSach.stream()
                .filter(s -> trangThai == null || trangThai.isBlank() || trangThai.trim().equals(s.getTrangThai()))
                .map(this::chuyenDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SanPhamQuanLyResponse layTheoId(Long id) {
        return chuyenDto(timTheoId(id));
    }

    @Transactional(readOnly = true)
    public List<KichThuocDto> layKichThuoc() {
        return kichThuocRepository.findAllByOrderByIdAsc().stream()
                .map(k -> new KichThuocDto(k.getId(), k.getMaKichThuoc(), k.getTenKichThuoc()))
                .toList();
    }

    @Transactional
    public SanPhamQuanLyResponse them(SanPhamQuanLyRequest yeuCau) {
        SanPham sanPham = new SanPham();
        sanPham.setMaSanPham(layMa(yeuCau.maSanPham()));
        apDungThongTin(sanPham, yeuCau, 0L);
        List<DongGia> cacGia = kiemTraGia(yeuCau.cacGia()); // kiểm tra xong hết rồi mới ghi vào database
        SanPham daLuu = luu(sanPham);
        luuGia(daLuu, cacGia);
        return chuyenDto(daLuu);
    }

    @Transactional
    public SanPhamQuanLyResponse sua(Long id, SanPhamQuanLyRequest yeuCau) {
        SanPham sanPham = timTheoId(id); // mã sản phẩm giữ nguyên, không sửa
        apDungThongTin(sanPham, yeuCau, id);
        List<DongGia> cacGia = kiemTraGia(yeuCau.cacGia());
        SanPham daLuu = luu(sanPham);
        luuGia(daLuu, cacGia);
        return chuyenDto(daLuu);
    }

    /** "Xóa" = ngừng bán. Không xóa dòng vì đơn hàng cũ còn tham chiếu sản phẩm này. */
    @Transactional
    public SanPhamQuanLyResponse ngungBan(Long id) {
        SanPham sanPham = timTheoId(id);
        sanPham.setTrangThai(NGUNG_BAN);
        return chuyenDto(luu(sanPham));
    }

    // idHienTai = id đang sửa (0 khi thêm mới), dùng để kiểm tra trùng tên trong cùng danh mục
    private void apDungThongTin(SanPham sanPham, SanPhamQuanLyRequest yeuCau, Long idHienTai) {
        String ten = KiemTra.chuoiBatBuoc(yeuCau.tenSanPham(), "Tên sản phẩm", 150);
        String moTa = KiemTra.chuoiTuyChon(yeuCau.moTa(), "Mô tả", 1000);
        String hinhAnh = KiemTra.chuoiTuyChon(yeuCau.hinhAnh(), "Hình ảnh", 255);
        String macDinh = sanPham.getTrangThai() == null ? DANG_BAN : sanPham.getTrangThai();
        String trangThai = KiemTra.trangThai(yeuCau.trangThai(), macDinh, DANG_BAN, NGUNG_BAN);

        if (yeuCau.danhMucId() == null) {
            throw QuanLyException.saiDuLieu("Vui lòng chọn danh mục");
        }
        DanhMuc danhMuc = danhMucRepository.findById(yeuCau.danhMucId())
                .orElseThrow(() -> QuanLyException.saiDuLieu("Danh mục không tồn tại"));
        if (DANG_BAN.equals(trangThai) && !DANG_BAN.equals(danhMuc.getTrangThai())) {
            throw QuanLyException.saiDuLieu("Danh mục đang ngừng bán, không thể bán sản phẩm trong danh mục này");
        }
        if (sanPhamRepository.existsByTenSanPhamIgnoreCaseAndDanhMuc_IdAndIdNot(ten, danhMuc.getId(), idHienTai)) {
            throw QuanLyException.saiDuLieu("Sản phẩm \"" + ten + "\" đã có trong danh mục này");
        }

        sanPham.setTenSanPham(ten);
        sanPham.setMoTa(moTa);
        sanPham.setHinhAnh(hinhAnh);
        sanPham.setTrangThai(trangThai);
        sanPham.setDanhMuc(danhMuc);
    }

    // Mã để trống thì tự sinh SP + số thứ tự kế tiếp (SP008, SP009...)
    private String layMa(String maYeuCau) {
        if (maYeuCau == null || maYeuCau.isBlank()) {
            return taoMaMoi();
        }
        String ma = maYeuCau.trim().toUpperCase();
        if (!ma.matches("[A-Z0-9_-]{1,20}")) {
            throw QuanLyException.saiDuLieu("Mã sản phẩm chỉ gồm chữ không dấu, số, dấu - hoặc _ (tối đa 20 ký tự)");
        }
        if (sanPhamRepository.existsByMaSanPhamIgnoreCase(ma)) {
            throw QuanLyException.saiDuLieu("Mã sản phẩm \"" + ma + "\" đã tồn tại");
        }
        return ma;
    }

    private String taoMaMoi() {
        int lonNhat = 0;
        for (SanPham s : sanPhamRepository.findAll()) {
            String ma = s.getMaSanPham();
            if (ma != null && ma.toUpperCase().matches("SP\\d{1,9}")) {
                lonNhat = Math.max(lonNhat, Integer.parseInt(ma.substring(2)));
            }
        }
        String ma;
        do {
            lonNhat++;
            ma = String.format("SP%03d", lonNhat);
        } while (sanPhamRepository.existsByMaSanPhamIgnoreCase(ma));
        return ma;
    }

    // Bảng giá: ít nhất một size, không khai báo trùng size, giá là số nguyên lớn hơn 0
    private List<DongGia> kiemTraGia(List<GiaSizeRequest> cacGia) {
        if (cacGia == null || cacGia.isEmpty()) {
            throw QuanLyException.saiDuLieu("Sản phẩm phải có giá cho ít nhất một size");
        }
        List<DongGia> ketQua = new ArrayList<>();
        Set<Long> daCo = new HashSet<>();
        for (GiaSizeRequest g : cacGia) {
            if (g == null || g.kichThuocId() == null) {
                throw QuanLyException.saiDuLieu("Thiếu kích thước trong bảng giá");
            }
            if (!daCo.add(g.kichThuocId())) {
                throw QuanLyException.saiDuLieu("Một size không được khai báo giá hai lần");
            }
            KichThuoc kichThuoc = kichThuocRepository.findById(g.kichThuocId())
                    .orElseThrow(() -> QuanLyException.saiDuLieu("Kích thước không tồn tại (id " + g.kichThuocId() + ")"));
            BigDecimal gia = KiemTra.soTienNguyen(g.giaBan(), "Giá size " + kichThuoc.getTenKichThuoc(), false);
            ketQua.add(new DongGia(kichThuoc, gia));
        }
        return ketQua;
    }

    // Đồng bộ bảng san_pham_kich_thuoc: size đã có thì cập nhật giá, size mới thì thêm, size bị bỏ thì xóa
    private void luuGia(SanPham sanPham, List<DongGia> cacGiaMoi) {
        List<SanPhamKichThuoc> hienCo = giaRepository.findBySanPham_IdOrderByKichThuoc_IdAsc(sanPham.getId());
        Map<Long, SanPhamKichThuoc> theoSize = new HashMap<>();
        for (SanPhamKichThuoc g : hienCo) {
            theoSize.put(g.getKichThuoc().getId(), g);
        }

        Set<Long> giuLai = new HashSet<>();
        for (DongGia dong : cacGiaMoi) {
            Long kichThuocId = dong.kichThuoc().getId();
            giuLai.add(kichThuocId);
            SanPhamKichThuoc g = theoSize.get(kichThuocId);
            if (g == null) {
                g = new SanPhamKichThuoc();
                g.setSanPham(sanPham);
                g.setKichThuoc(dong.kichThuoc());
            }
            g.setGiaBan(dong.giaBan());
            giaRepository.save(g);
        }

        List<SanPhamKichThuoc> canXoa = hienCo.stream()
                .filter(g -> !giuLai.contains(g.getKichThuoc().getId()))
                .toList();
        giaRepository.deleteAll(canXoa);
        giaRepository.flush();
    }

    private SanPham luu(SanPham sanPham) {
        try {
            return sanPhamRepository.saveAndFlush(sanPham);
        } catch (DataIntegrityViolationException e) {
            throw QuanLyException.saiDuLieu("Không lưu được sản phẩm (mã sản phẩm có thể đã tồn tại)");
        }
    }

    private SanPham timTheoId(Long id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> QuanLyException.khongTimThay("Không tìm thấy sản phẩm id = " + id));
    }

    private SanPhamQuanLyResponse chuyenDto(SanPham s) {
        List<KichThuocGiaDto> cacSize = giaRepository.findBySanPham_IdOrderByKichThuoc_IdAsc(s.getId()).stream()
                .map(g -> new KichThuocGiaDto(g.getKichThuoc().getId(), g.getKichThuoc().getTenKichThuoc(), g.getGiaBan()))
                .toList();
        return new SanPhamQuanLyResponse(s.getId(), s.getMaSanPham(), s.getDanhMuc().getId(),
                s.getDanhMuc().getTenDanhMuc(), s.getTenSanPham(), s.getMoTa(), s.getHinhAnh(),
                s.getTrangThai(), cacSize);
    }
}