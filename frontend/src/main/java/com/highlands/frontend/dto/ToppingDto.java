package com.highlands.frontend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ToppingDto(Long id, String tenTopping, BigDecimal giaBan, String hinhAnh) {
}