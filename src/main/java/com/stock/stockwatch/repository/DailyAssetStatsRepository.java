package com.stock.stockwatch.repository;

import com.stock.stockwatch.entity.DailyAssetStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyAssetStatsRepository extends JpaRepository<DailyAssetStats, Long> {
    List<DailyAssetStats> findByAssetTypeAndTradeTypeOrderByRankingAsc(String assetType, String tradeType);
}
