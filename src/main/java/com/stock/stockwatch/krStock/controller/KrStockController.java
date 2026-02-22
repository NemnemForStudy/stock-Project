package com.stock.stockwatch.krStock.controller;

import com.stock.stockwatch.common.AssetDto;
import com.stock.stockwatch.krStock.service.KrStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks/kr")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class KrStockController {
    private final KrStockService krStockService;

    @GetMapping("/top10")
    public Map<String, List<AssetDto>> getTop10() {
        return krStockService.getKrStockTop10();
    }
}
