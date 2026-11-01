package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.DonHang;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {

    // 100 đơn mới nhất
    List<DonHang> findTop100ByOrderByThoiGianDatDesc();

    // Đơn theo trạng thái, đơn đặt trước xếp trước (thứ tự pha chế)
    List<DonHang> findTop100ByTrangThaiOrderByThoiGianDatAsc(String trangThai);
}