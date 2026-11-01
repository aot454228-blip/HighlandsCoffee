package com.highlands.backend.dto;

import java.math.BigDecimal;

public record ToppingDto(Long id, String tenTopping, BigDecimal giaBan, String hinhAnh) {
}