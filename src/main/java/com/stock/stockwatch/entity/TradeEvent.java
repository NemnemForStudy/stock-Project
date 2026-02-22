package com.stock.stockwatch.entity; // 패키지 선언 변경

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "trade_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private double tradePrice;

    @Column(nullable = false)
    private double tradeVolume;

    @Column(nullable = false)
    private double totalKrw;

    @Column(nullable = false)
    private LocalDateTime tradeTimestamp;

    public TradeEvent(String code, double tradePrice, double tradeVolume, double totalKrw, long timestamp) {
        this.code = code;
        this.tradePrice = tradePrice;
        this.tradeVolume = tradeVolume;
        this.totalKrw = totalKrw;
        this.tradeTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
    }
}
