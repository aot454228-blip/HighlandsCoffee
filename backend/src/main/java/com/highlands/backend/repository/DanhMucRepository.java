package com.highlands.backend.repository;

import com.highlands.backend.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {

    // --- Bổ sung cho phần quản lý ---
    java.util.List<DanhMuc> findAllByOrderByIdAsc();

    boolean existsByTenDanhMucIgnoreCaseAndIdNot(String tenDanhMuc, Long id);
}
