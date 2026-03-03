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

    @Value("${exchange_rate_key}")
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
    public void updateExchangeRates() {
        log.info("ExchangeRate-API를 통한 환율 갱신 시작");

        // KRW 기준으로 모든 환율 정보를 한 번에 가져옴
        String url = String.format("https://v6.exchangerate-api.com/v6/%s/latest/KRW", apiKey);
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "success".equals(response.get("result"))) {
                Map<String, Object> rates = (Map<String, Object>) response.get("conversion_rates");

                for (String currency : targetCurrencies) {
                    if (rates.containsKey(currency)) {
                        // KIS 방식(1 / 외화)으로 계산하여 KRW 가격 추출
                        double rateToKrw = 1.0 / Double.parseDouble(rates.get(currency).toString());
                        cachedRates.put(currency, Math.round(rateToKrw * 100.0) / 100.0);
                    }
                }
                updateRedisCache();
                log.info(">>>> 환율 갱신 완료: {}", cachedRates);
            }
        } catch (Exception e) {
            log.error("환율 API 호출 실패: {}", e.getMessage());
        }
    }

    private void updateRedisCache() {
        if(!cachedRates.isEmpty() && cacheManager.getCache("exchangeRates") != null) {
            cacheManager.getCache("exchangeRates").put("allRates", new HashMap<>(cachedRates));
        }
    }
}
