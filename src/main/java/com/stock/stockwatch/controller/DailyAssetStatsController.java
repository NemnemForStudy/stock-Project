package com.stock.stockwatch.controller;

import com.stock.stockwatch.entity.DailyAssetStats;
import com.stock.stockwatch.repository.DailyAssetStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:3000") // 프론트 포트 허용
public class DailyAssetStatsController {
    @Autowired
    private DailyAssetStatsRepository dailyAssetStatsRepository;

    @GetMapping
    public List<DailyAssetStats> getStats(
            @RequestParam String assetType,
            @RequestParam String tradeType) {
        return dailyAssetStatsRepository.findByAssetTypeAndTradeTypeOrderByRankingAsc(assetType, tradeType);
    }
}
