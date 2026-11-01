package com.highlands.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.KichThuocGiaDto;
import com.highlands.backend.dto.SanPhamDto;
import com.highlands.backend.dto.ToppingDto;
import com.highlands.backend.entity.SanPham;
import com.highlands.backend.entity.SanPhamKichThuoc;
import com.highlands.backend.entity.Topping;
import com.highlands.backend.repository.SanPhamRepository;
import com.highlands.backend.repository.ToppingRepository;

@Service
@Transactional(readOnly = true)
public class ThucDonService {

    private static final String DANG_BAN = "DANG_BAN";

    private final SanPhamRepository sanPhamRepository;
    private final ToppingRepository toppingRepository;

    public ThucDonService(SanPhamRepository sanPhamRepository, ToppingRepository toppingRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.toppingRepository = toppingRepository;
    }

    public List<SanPhamDto> layThucDon() {
        List<SanPhamDto> ketQua = new ArrayList<>();
        for (SanPham sp : sanPhamRepository.findByTrangThaiOrderByTenSanPhamAsc(DANG_BAN)) {
            List<KichThuocGiaDto> cacSize = new ArrayList<>();
            for (SanPhamKichThuoc gia : sp.getCacGiaTheoSize()) {
                cacSize.add(new KichThuocGiaDto(
                        gia.getKichThuoc().getId(),
                        gia.getKichThuoc().getTenKichThuoc(),
                        gia.getGiaBan()));
            }
            // Sắp xếp các size theo giá tăng dần
            cacSize.sort(Comparator.comparing(KichThuocGiaDto::giaBan));

            ketQua.add(new SanPhamDto(
                    sp.getId(),
                    sp.getMaSanPham(),
                    sp.getTenSanPham(),
                    sp.getDanhMuc().getId(),
                    sp.getDanhMuc().getTenDanhMuc(),
                    sp.getMoTa(),
                    sp.getHinhAnh(),
                    cacSize));
        }
        return ketQua;
    }

    public List<ToppingDto> layTopping() {
        List<ToppingDto> ketQua = new ArrayList<>();
        for (Topping t : toppingRepository.findByTrangThaiOrderByTenToppingAsc(DANG_BAN)) {
            ketQua.add(new ToppingDto(t.getId(), t.getTenTopping(), t.getGiaBan(), t.getHinhAnh()));
        }
        return ketQua;
    }
}