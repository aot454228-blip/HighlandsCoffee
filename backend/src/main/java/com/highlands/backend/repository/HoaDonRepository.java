package com.highlands.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.HoaDon;

public interface HoaDonRepository extends JpaRepository<HoaDon, Long> {

    boolean existsByDonHangId(Long donHangId);

    Optional<HoaDon> findByDonHangId(Long donHangId);
}