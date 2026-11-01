package com.highlands.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.ThanhToan;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {

    boolean existsByMaGiaoDich(String maGiaoDich);

    Optional<ThanhToan> findFirstByHoaDonId(Long hoaDonId);
}