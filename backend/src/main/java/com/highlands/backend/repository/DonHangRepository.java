package com.highlands.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.DonHang;

public interface DonHangRepository extends JpaRepository<DonHang, Long> {
}