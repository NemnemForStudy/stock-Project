package com.stock.stockwatch.usStock.controller;

import com.stock.stockwatch.common.AssetDto;
import com.stock.stockwatch.usStock.service.UsStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks/us")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UsStockController {
    private final UsStockService usStockService;

    @GetMapping("/top10")
    public Map<String, List<AssetDto>> getTop10() {
        return usStockService.getUsStockTop10();
    }
}
