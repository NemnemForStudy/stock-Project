package com.stock.stockwatch.exchangeRate.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final CacheManager cacheManager; // 캐시 매니저 주입

    // 실시간 데이터를 담아둘 메모리 공간 (캐시)
    private final Map<String, Double> cachedRates = new ConcurrentHashMap<>();

    @Value("${alpha_vantage_exchange_rate_key}")
    private String apiKey;

    // 프론트엔드에서 필요한 통화 목록
    private final List<String> targetCurrencies = Arrays.asList("USD", "EUR", "JPY", "CNY", "GBP");

    @PostConstruct
    public void init() {
        // 별도 스레드에서 실행해 서버 기동 속도에 영향 안주게
        new Thread(this::updateExchangeRates).start();
    }

    @Cacheable(value = "exchangeRates", key = "'allRates'", unless = "#result.isEmpty()")
    public Map<String, Double> getAllExchangeRates() {
        // 캐시가 비어있을 경우를 대비해 초기 데이터가 없다면 한 번 실행 유도 가능
        if (cachedRates.isEmpty()) {
            log.warn("Cache is empty. Waiting for scheduler or manual trigger.");
        }
        return cachedRates;
    }
    /**
     * 스케줄러: 10분마다 환율을 자동으로 업데이트합니다.
     * Alpha Vantage 무료 제한(분당 5회)을 피하기 위해 통화별로 15초 간격을 두고 호출합니다.
     */
    @Scheduled(fixedRate = 600000) // 10분마다 실행
    public void updateExchangeRates() {
        log.info("Starting scheduled exchange rate update");

        for (String currency : targetCurrencies) {
            fetchAndCacheRate(currency);

            // 각 통화 정보를 가져올 때마다 Redis 캐시 실시간 업데이트
            updateRedisCache();

            // API 제한 방지를 위한 15초 대기
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Exchange rate update completed: {}", cachedRates);
        }
    }

    private void fetchAndCacheRate(String from) {
        String url = String.format(
                "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=%s&to_currency=KRW&apikey=%s",
                from, apiKey
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("Realtime Currency Exchange Rate")) {
                Map<String, String> data = (Map<String, String>) response.get("Realtime Currency Exchange Rate");
                double rate = Double.parseDouble(data.get("5. Exchange Rate"));
                cachedRates.put(from, rate); // 예: "USD" -> 1342.5
            } else {
                log.warn("Failed to fetch rate for {}: {}", from, response);
            }
        } catch (Exception e) {
            log.error("Error fetching Alpha Vantage rate for {}", from, e);
        }
    }

    private void updateRedisCache() {
        if(!cachedRates.isEmpty() && cacheManager.getCache("exchangeRates") != null) {
            cacheManager.getCache("exchangeRates").put("allRates", new HashMap<>(cachedRates));
        }
    }
}
