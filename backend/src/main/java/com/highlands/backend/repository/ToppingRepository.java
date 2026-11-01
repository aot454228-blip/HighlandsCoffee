package com.highlands.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.highlands.backend.entity.Topping;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findByTrangThaiOrderByTenToppingAsc(String trangThai);
}