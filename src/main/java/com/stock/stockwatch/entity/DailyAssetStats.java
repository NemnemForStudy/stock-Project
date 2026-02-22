package com.stock.stockwatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "daily_asset_stats")
public class DailyAssetStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetType;
    private String tradeType;
    
    @Column(name = "rank")
    private Integer ranking;

    private String name;
    private String code;
    private String currentPrice;
    private String changeRate;
    private String volumeKrw; // 거래대금
}
