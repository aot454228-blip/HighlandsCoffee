package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.KichThuoc;

public interface KichThuocRepository extends JpaRepository<KichThuoc, Long> {

    List<KichThuoc> findAllByOrderByIdAsc();
}