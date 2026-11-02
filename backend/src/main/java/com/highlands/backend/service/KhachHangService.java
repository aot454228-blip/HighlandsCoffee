package com.highlands.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.highlands.backend.dto.KhachHangDto;
import com.highlands.backend.entity.KhachHang;
import com.highlands.backend.repository.KhachHangRepository;

@Service
public class KhachHangService {

    private static final int TOI_DA = 50; // chỉ trả tối đa 50 kết quả

    private final KhachHangRepository khachHangRepository;

    public KhachHangService(KhachHangRepository khachHangRepository) {
        this.khachHangRepository = khachHangRepository;
    }

    @Transactional(readOnly = true)
    public List<KhachHangDto> timKiem(String tuKhoa) {
        List<KhachHang> cacKhach = (tuKhoa == null || tuKhoa.isBlank())
                ? khachHangRepository.findTop50ByOrderByHoTenAsc()
                : khachHangRepository.timKiem(tuKhoa.trim());

        return cacKhach.stream()
                .limit(TOI_DA)
                .map(this::sangDto)
                .toList();
    }

    private KhachHangDto sangDto(KhachHang k) {
        return new KhachHangDto(k.getId(), k.getMaKhachHang(), k.getHoTen(), k.getSoDienThoai(),
                k.getEmail(), k.getNgaySinh(), k.getGioiTinh(), k.getDiaChi(), k.getDiemTichLuy());
    }
}
