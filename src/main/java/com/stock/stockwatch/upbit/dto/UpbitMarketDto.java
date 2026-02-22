package com.stock.stockwatch.upbit.dto;

// record 하는 이유 -> 데이터 전달 전용 클래스임. 쉽게 말해 데이터 담을 그릇임.
public record UpbitMarketDto (
    String market,
    String koreanName,
    Double tradePrice24h,
    Double currentPrice,    // trade_price
    Double changeRate,
    String change
) {}
