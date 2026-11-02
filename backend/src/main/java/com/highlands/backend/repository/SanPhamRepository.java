package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.SanPham;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    List<SanPham> findByTrangThaiOrderByTenSanPhamAsc(String trangThai);

    // --- Bổ sung cho phần quản lý ---
    java.util.List<SanPham> findAllByOrderByIdAsc();

    java.util.List<SanPham> findByDanhMuc_IdOrderByIdAsc(Long danhMucId);

    java.util.List<SanPham> findByTenSanPhamContainingIgnoreCaseOrderByIdAsc(String tuKhoa);

    java.util.List<SanPham> findByTenSanPhamContainingIgnoreCaseAndDanhMuc_IdOrderByIdAsc(String tuKhoa, Long danhMucId);

    boolean existsByDanhMuc_IdAndTrangThai(Long danhMucId, String trangThai);

    boolean existsByTenSanPhamIgnoreCaseAndDanhMuc_IdAndIdNot(String tenSanPham, Long danhMucId, Long id);

    boolean existsByMaSanPhamIgnoreCase(String maSanPham);
}
