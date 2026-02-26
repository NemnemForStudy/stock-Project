package com.stock.stockwatch.upbit.service;

/*
* REST API를 호출해 상위 10개 코인 리스트를 뽑아주는 역할
* */

import com.stock.stockwatch.common.AssetDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service // 비즈니스 로직을 담당하는 서비스 객체임을 스프링에 알림.
public class UpbitMarketService {
    // RestTemplate : 스프링에서 제공하는 HTTP 통신 도구임.
    // 브라우저 주소창을 치는 것처럼, 자바가 API에 접속하게 해줌.
    private final RestTemplate restTemplate;
    private Map<String, List<AssetDto>> cachedResult = null;
    private LocalDateTime lastFetched = LocalDateTime.MIN;
    private static final int CACHE_MINUTES = 1; // 1분 캐시

    // 이렇게 RestTemplateBuilder를 주입받아 빌드해야 @RestClientTest가 인식함.
    public UpbitMarketService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     *업비트에서 거래대금이 가장 많은 상위 10개 코인의 코드를 가져옴.
     * */
    public Map<String, List<AssetDto>> getTop10MarketCodes() {
        // 캐시가 유효하면 바로 반환
        if (cachedResult != null && lastFetched.isAfter(LocalDateTime.now().minusMinutes(CACHE_MINUTES))) {
            return cachedResult;
        }

        try {
            String marketUrl = "https://api.upbit.com/v1/market/all";
            List<Map<String, String>> allMarkets = restTemplate.getForObject(marketUrl, List.class);
            if (allMarkets == null) return Collections.emptyMap();

            Map<String, String> nameMap = allMarkets.stream()
                    .filter(m -> m.get("market").startsWith("KRW-"))
                    .collect(Collectors.toMap(
                            m -> m.get("market"),
                            m -> m.get("korean_name")
                    ));

            String krwMarketCodes = String.join(",", nameMap.keySet());
            String tickerUrl = "https://api.upbit.com/v1/ticker?markets=" + krwMarketCodes;
            List<Map<String, Object>> tickerList = restTemplate.getForObject(tickerUrl, List.class);

            List<AssetDto> allAssets = tickerList.stream()
                    .map(t -> new AssetDto(
                            t.get("market").toString().split("-")[1],
                            nameMap.get(t.get("market").toString()),
                            Double.valueOf(t.get("trade_price").toString()),
                            Double.valueOf(t.get("signed_change_rate").toString()),
                            t.get("change").toString(),
                            Double.valueOf(t.get("acc_trade_price_24h").toString())
                    )).toList();

            // 캐시 저장
            cachedResult = AssetDto.createResponse(
                    filterAndSort(allAssets, true),
                    filterAndSort(allAssets, false)
            );
            lastFetched = LocalDateTime.now();

            return cachedResult;

        } catch (Exception e) {
            // 429 등 에러 발생 시 캐시가 있으면 캐시 반환, 없으면 빈 맵
            if (cachedResult != null) {
                System.out.println("API 오류 - 캐시 데이터 반환: " + e.getMessage());
                return cachedResult;
            }
            return Collections.emptyMap();
        }
    }

    private List<AssetDto> filterAndSort(List<AssetDto> list, boolean isBuy) {
        return list.stream()
                .filter(a -> isBuy ? !a.changeType().equals("FALL") : a.changeType().equals("FALL"))
                .sorted((a, b) -> b.volume().compareTo(a.volume()))
                .limit(10)
                .toList();
    }

}
