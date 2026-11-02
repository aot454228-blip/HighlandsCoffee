package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.SanPhamKichThuoc;

public interface SanPhamKichThuocRepository extends JpaRepository<SanPhamKichThuoc, Long> {

    // Bảng giá theo size của một sản phẩm
    List<SanPhamKichThuoc> findBySanPham_IdOrderByKichThuoc_IdAsc(Long sanPhamId);
}