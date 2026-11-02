package com.highlands.backend.dto;

import java.math.BigDecimal;

/** Giá của sản phẩm ở một size. */
public record GiaSizeRequest(Long kichThuocId, BigDecimal giaBan) {
}