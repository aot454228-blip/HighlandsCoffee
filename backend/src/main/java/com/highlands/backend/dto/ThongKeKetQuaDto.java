package com.highlands.backend.dto;

import java.util.List;

/** Tổng tiền cả khoảng ngày và chi tiết từng ngày. */
public record ThongKeKetQuaDto(long tong, List<ThongKeNgayDto> theoNgay) {
}