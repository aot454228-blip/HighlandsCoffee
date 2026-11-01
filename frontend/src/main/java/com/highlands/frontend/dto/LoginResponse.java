package com.highlands.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(Long id, String tenDangNhap, String vaiTro) {
}