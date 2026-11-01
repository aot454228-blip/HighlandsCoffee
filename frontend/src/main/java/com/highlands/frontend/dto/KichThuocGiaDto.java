package com.highlands.frontend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KichThuocGiaDto(Long kichThuocId, String tenKichThuoc, BigDecimal giaBan) {
}