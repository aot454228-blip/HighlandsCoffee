package com.highlands.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.BanChayDto;
import com.highlands.backend.dto.ThongKeKetQuaDto;
import com.highlands.backend.service.ThongKeService;

@RestController
@RequestMapping("/api/quan-ly/thong-ke")
public class ThongKeController {

    private final ThongKeService service;

    public ThongKeController(ThongKeService service) {
        this.service = service;
    }

    // Ngày có dạng yyyy-MM-dd, ví dụ ?tuNgay=2026-10-01&denNgay=2026-10-31. Bỏ trống = 30 ngày gần nhất
    @GetMapping("/doanh-thu")
    public ThongKeKetQuaDto doanhThu(
            @RequestParam(name = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.doanhThu(tuNgay, denNgay);
    }

    @GetMapping("/chi-phi")
    public ThongKeKetQuaDto chiPhi(
            @RequestParam(name = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return service.chiPhi(tuNgay, denNgay);
    }

    @GetMapping("/ban-chay")
    public List<BanChayDto> banChay(
            @RequestParam(name = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(name = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(name = "top", defaultValue = "5") int top) {
        return service.banChay(tuNgay, denNgay, top);
    }
}