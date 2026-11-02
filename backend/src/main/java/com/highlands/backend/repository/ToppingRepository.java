package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.Topping;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findByTrangThaiOrderByTenToppingAsc(String trangThai);

    // --- Bổ sung cho phần quản lý ---
    java.util.List<Topping> findAllByOrderByIdAsc();

    boolean existsByTenToppingIgnoreCaseAndIdNot(String tenTopping, Long id);
}
