package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.SanPham;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {

    List<SanPham> findByTrangThaiOrderByTenSanPhamAsc(String trangThai);
}