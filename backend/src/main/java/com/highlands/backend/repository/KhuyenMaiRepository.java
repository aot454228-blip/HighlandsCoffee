package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.KhuyenMai;

public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Long> {

    List<KhuyenMai> findAllByOrderByIdAsc();

    List<KhuyenMai> findByTenKhuyenMaiContainingIgnoreCaseOrderByIdAsc(String tuKhoa);

    // true nếu đã có khuyến mãi khác (id khác) dùng mã này
    boolean existsByMaCodeIgnoreCaseAndIdNot(String maCode, Long id);
}