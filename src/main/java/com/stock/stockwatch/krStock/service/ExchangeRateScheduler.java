package com.stock.stockwatch.krStock.service;

import com.stock.stockwatch.exchangeRate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateScheduler {
    private final ExchangeRateService exchangeRateService;

    // 30분 마다 환율 업데이트(하루 48회 호출)
    @Scheduled(fixedRate = 2600000)
    public void updateRates() {
        log.info("정기 환율 업데이트 시작");
        exchangeRateService.getAllExchangeRates();
    }
}
