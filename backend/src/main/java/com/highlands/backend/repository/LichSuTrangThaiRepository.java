package com.highlands.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.LichSuTrangThaiDonHang;

public interface LichSuTrangThaiRepository extends JpaRepository<LichSuTrangThaiDonHang, Long> {
}