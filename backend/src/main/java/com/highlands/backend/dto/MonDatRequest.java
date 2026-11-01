package com.highlands.backend.dto;

import java.util.List;

/** Một món khách gọi. Chú ý: KHÔNG có giá - giá do backend tự tra trong database. */
public record MonDatRequest(
        Long sanPhamId,
        Long kichThuocId,
        int soLuong,
        List<Long> cacToppingId,
        String mucDuong,
        String mucDa,
        String ghiChu) {
}