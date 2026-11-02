package com.highlands.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.BanChayDto;
import com.highlands.backend.dto.ThongKeKetQuaDto;
import com.highlands.backend.dto.ThongKeNgayDto;
import com.highlands.backend.exception.QuanLyException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/** Thống kê chỉ đọc dữ liệu có sẵn, không tạo bảng hay entity mới. */
@Service
public class ThongKeService {

    // Doanh thu: chỉ tính đơn đã HOAN_THANH (giống view v_doanh_thu_theo_ngay)
    private static final String SQL_DOANH_THU =
            "SELECT DATE(thoi_gian_dat) AS ngay, SUM(tong_thanh_toan) AS tien "
            + "FROM don_hang "
            + "WHERE trang_thai = 'HOAN_THANH' AND thoi_gian_dat >= ?1 AND thoi_gian_dat < ?2 "
            + "GROUP BY DATE(thoi_gian_dat) ORDER BY ngay";

    // Chi phí: chỉ tính phiếu nhập đã nhập kho
    private static final String SQL_CHI_PHI =
            "SELECT ngay_nhap AS ngay, SUM(tong_tien) AS tien "
            + "FROM phieu_nhap "
            + "WHERE trang_thai = 'DA_NHAP_KHO' AND ngay_nhap BETWEEN ?1 AND ?2 "
            + "GROUP BY ngay_nhap ORDER BY ngay_nhap";

    private static final String SQL_BAN_CHAY =
            "SELECT ct.san_pham_id AS id, sp.ten_san_pham AS ten, "
            + "SUM(ct.so_luong) AS so_luong, SUM(ct.thanh_tien) AS doanh_thu "
            + "FROM chi_tiet_don_hang ct "
            + "JOIN don_hang dh ON dh.id = ct.don_hang_id "
            + "JOIN san_pham sp ON sp.id = ct.san_pham_id "
            + "WHERE dh.trang_thai = 'HOAN_THANH' AND dh.thoi_gian_dat >= ?1 AND dh.thoi_gian_dat < ?2 "
            + "GROUP BY ct.san_pham_id, sp.ten_san_pham "
            + "ORDER BY so_luong DESC, doanh_thu DESC LIMIT ?3";

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public ThongKeKetQuaDto doanhThu(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate[] k = khoang(tuNgay, denNgay);
        return gop(chay(SQL_DOANH_THU, k[0].atStartOfDay(), k[1].plusDays(1).atStartOfDay()));
    }

    @Transactional(readOnly = true)
    public ThongKeKetQuaDto chiPhi(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate[] k = khoang(tuNgay, denNgay);
        return gop(chay(SQL_CHI_PHI, k[0], k[1]));
    }

    @Transactional(readOnly = true)
    public List<BanChayDto> banChay(LocalDate tuNgay, LocalDate denNgay, int top) {
        LocalDate[] k = khoang(tuNgay, denNgay);
        int soDong = Math.max(1, Math.min(top, 50));
        List<BanChayDto> ketQua = new ArrayList<>();
        for (Object[] r : chay(SQL_BAN_CHAY, k[0].atStartOfDay(), k[1].plusDays(1).atStartOfDay(), soDong)) {
            ketQua.add(new BanChayDto(
                    ((Number) r[0]).longValue(),
                    String.valueOf(r[1]),
                    ((Number) r[2]).longValue(),
                    ((Number) r[3]).longValue()));
        }
        return ketQua;
    }

    // Không truyền ngày thì mặc định 30 ngày gần nhất
    private LocalDate[] khoang(LocalDate tuNgay, LocalDate denNgay) {
        LocalDate den = denNgay != null ? denNgay : LocalDate.now();
        LocalDate tu = tuNgay != null ? tuNgay : den.minusDays(30);
        if (tu.isAfter(den)) {
            throw QuanLyException.saiDuLieu("Từ ngày không được sau đến ngày");
        }
        return new LocalDate[] { tu, den };
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> chay(String sql, Object... thamSo) {
        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < thamSo.length; i++) {
            query.setParameter(i + 1, thamSo[i]);
        }
        return query.getResultList();
    }

    // Chuyển các dòng [ngày, tiền] thành {tong, theoNgay}
    private ThongKeKetQuaDto gop(List<Object[]> cacDong) {
        long tong = 0;
        List<ThongKeNgayDto> theoNgay = new ArrayList<>();
        for (Object[] r : cacDong) {
            long tien = ((Number) r[1]).longValue();
            tong += tien;
            theoNgay.add(new ThongKeNgayDto(String.valueOf(r[0]), tien));
        }
        return new ThongKeKetQuaDto(tong, theoNgay);
    }
}