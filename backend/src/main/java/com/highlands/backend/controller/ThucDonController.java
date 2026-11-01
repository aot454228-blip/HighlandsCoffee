package com.highlands.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.highlands.backend.dto.SanPhamDto;
import com.highlands.backend.dto.ToppingDto;
import com.highlands.backend.service.ThucDonService;

@RestController
@RequestMapping("/api")
public class ThucDonController {

    private final ThucDonService thucDonService;

    public ThucDonController(ThucDonService thucDonService) {
        this.thucDonService = thucDonService;
    }

    @GetMapping("/thuc-don")
    public List<SanPhamDto> layThucDon() {
        return thucDonService.layThucDon();
    }

    @GetMapping("/topping")
    public List<ToppingDto> layTopping() {
        return thucDonService.layTopping();
    }
}