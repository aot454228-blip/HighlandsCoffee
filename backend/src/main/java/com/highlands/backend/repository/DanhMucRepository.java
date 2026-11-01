package com.highlands.backend.repository;

import com.highlands.backend.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
}